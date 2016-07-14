package financemonitor.org.financemonitor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import financemonitor.org.financemonitor.adapter.MoveAdapter;
import financemonitor.org.financemonitor.dao.CategoryEntry;
import financemonitor.org.financemonitor.dao.FinanceDao;
import financemonitor.org.financemonitor.dao.MoveEntry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private final int readTimeout = 4000;
    private final int connTimeout = 8000;

    private List<MoveEntry> moves = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ListActivity.this, NewActivity.class));
                }
            });
        }

        ListView lst = (ListView) findViewById(R.id.lst_movements);
        if (lst == null) return;
        lst.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                handleDeleteMovement(position);
                return true;
            }
        });

        //synchronized categories when the app starts
        synchronize();
    }

    private void handleDeleteMovement(int position) {
        if (moves == null || moves.size() <= position)
            return;

        final int pos = position;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.msg_confirm));
        builder.setMessage(getString(R.string.msg_delete_question));

        builder.setPositiveButton(getString(R.string.msg_yes_up), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                FinanceDao dao = new FinanceDao(getApplicationContext());
                dao.open();
                if (!dao.deleteMove(moves.get(pos)))
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_delete_error), Toast.LENGTH_LONG).show();
                dao.close();
                updateList();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getString(R.string.msg_no_up), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(new Intent(ListActivity.this, SettingsActivity.class));
                break;
            case R.id.action_sync:
                upload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList () {
        FinanceDao dao = new FinanceDao(getApplicationContext());
        dao.open();
        moves = dao.getMoves();
        dao.close();
        ListView lst = (ListView) findViewById(R.id.lst_movements);
        View emptyList = findViewById(R.id.txt_list_empty);
        if (lst == null || emptyList == null)
            return;
        if (moves.size() > 0) {
            emptyList.setVisibility(View.GONE);
            lst.setVisibility(View.VISIBLE);
        } else {
            emptyList.setVisibility(View.VISIBLE);
            lst.setVisibility(View.GONE);
            return;
        }
        MoveAdapter adapter = new MoveAdapter(this, android.R.layout.simple_list_item_1, moves);
        lst.setAdapter(adapter);
    }

    private void synchronize () {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String serverAddress = prefs.getString("server_address", "");
            new ServerSyncCategory().execute(serverAddress, "/app/cat.php");
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_no_network), Toast.LENGTH_LONG).show();
        }
    }

    private void upload () {
        if (moves.size() == 0)
            return;
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String serverAddress = prefs.getString("server_address", "");
            String username = prefs.getString("server_account", "username");
            new ServerSyncMovement().execute(serverAddress, "/app/pending.php", username);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_no_network), Toast.LENGTH_LONG).show();
        }
    }

    private class ServerSyncMovement extends AsyncTask<String, Void, SyncResult> {
        @Override
        protected SyncResult doInBackground(String... urls) {
            return SyncMovements(urls[0]+urls[1], urls[2]);
        }

        @Override
        protected void onPostExecute(SyncResult result) {
            String msg;
            if (result.getTotal() == 0)
                msg = getString(R.string.toast_no_move);
            else if (result.getError() == 0)
                msg = getString(R.string.toast_all_syncs);
            else if (result.getError() == result.getTotal())
                msg = getString(R.string.toast_no_server);
            else
                msg = getString(R.string.msg_sync) + " " +
                        String.valueOf(result.getTotal() - result.getError()) + " " +
                        getString(R.string.msg_out_of) + " " + String.valueOf(result.getTotal());
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            updateList();
        }
    }

    private class SyncResult {
        SyncResult(int e, int t) {
            error = e;
            total = t;
        }
        public int getError() {
            return error;
        }

        public int getTotal() {
            return total;
        }

        private int error = 0;
        private int total = 0;
    }

    private SyncResult SyncMovements (String urlServer, String username) {
        InputStream is;
        int error = 0;
        int total = 0;
        FinanceDao dao = new FinanceDao(getApplicationContext());
        dao.open();
        List<MoveEntry> act = dao.getMoves();
        for (MoveEntry m: act) {
            try {
                String addr = urlServer;
                addr += "?amount=" + String.valueOf(m.getAmount());
                addr += "&currency=" + String.valueOf(m.getCurrency());
                addr += "&user=" + username;
                addr += "&id_entry=" + String.valueOf(m.getCategory().getIdEntry());
                addr += "&when=" + String.valueOf(m.getWhen().getTime());
                URL url = new URL(addr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(readTimeout);
                conn.setConnectTimeout(connTimeout);
                conn.setRequestMethod("GET");
                conn.connect();
                is = conn.getInputStream();
                String contentAsString = readIt(is, 50);
                if (contentAsString.trim().compareTo("1") == 0)
                    dao.deleteMove(m);
                else
                    error++;
            } catch (IOException e) {
                error++;
            }
            total++;
        }
        dao.close();
        return new SyncResult(error, total);
    }

    private class ServerSyncCategory extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return SyncCategory(urls[0]+urls[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            String[] arr = result.split("\n");
            if (result.isEmpty() || arr.length == 0)
                return;
            FinanceDao dao = new FinanceDao(getApplicationContext());
            dao.open();
            dao.emptyCategories();
            for (String s : arr) {
                String[] data = s.split("-");
                if (s.isEmpty() || data.length != 3)
                    continue;
                CategoryEntry cat = new CategoryEntry();
                cat.setIdEntry(Long.valueOf(data[0].trim()));
                cat.setEntry(data[1].trim());
                cat.setCategory(data[2].trim());
                dao.addCategory(cat);
            }
            dao.close();
        }
    }

    private String SyncCategory (String myurl)  {
        InputStream is;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(readTimeout);
            conn.setConnectTimeout(connTimeout);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            return readIt(is, 500);
        } catch (java.net.MalformedURLException e) {
            return "";
        } catch (IOException e) {
            return "";
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

}
