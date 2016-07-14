package financemonitor.org.financemonitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import financemonitor.org.financemonitor.dao.CategoryEntry;
import financemonitor.org.financemonitor.dao.FinanceDao;
import financemonitor.org.financemonitor.dao.MoveEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NewActivity extends AppCompatActivity {

    private List<Long> ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new);
        setSupportActionBar(toolbar);

        // currency
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int pref_curr = Integer.valueOf(prefs.getString("currency_list", "0")) % 3;
        List<String> data = new ArrayList<>();
        data.add(getString(R.string.msg_euro));
        data.add(getString(R.string.msg_dollar));
        data.add(getString(R.string.msg_pound));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                                        android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.input_new_currency);
        if (spinner != null) {
            spinner.setAdapter(adapter);
            spinner.setSelection(pref_curr);
        }

        // categories
        FinanceDao dao = new FinanceDao(getApplicationContext());
        dao.open();
        List<CategoryEntry> categories = dao.getCategories();
        dao.close();
        data = new ArrayList<>();
        ids = new ArrayList<>();
        for(CategoryEntry cat : categories) {
            data.add(cat.getCategory() + " - " + cat.getEntry());
            ids.add(cat.getIdEntry());
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) findViewById(R.id.input_new_category);
        if (spinner != null)
            spinner.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (save()) {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_move_added), Toast.LENGTH_LONG).show();
                finish();
            } else {
                View v = getCurrentFocus();
                if (v != null) {
                    Snackbar.make(v, getString(R.string.toast_no_input),
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean save () {
        MoveEntry move = new MoveEntry();
        CategoryEntry cat = new CategoryEntry();

        // read and check inputs
        Spinner spinner = (Spinner) findViewById(R.id.input_new_category);
        if (ids.size() == 0 || spinner == null)
            return false;
        cat.setIdEntry(ids.get(spinner.getSelectedItemPosition()));
        move.setCategory(cat);

        TextView txt = (TextView) findViewById(R.id.input_new_move_amount);
        if (txt == null)
            return false;
        try {
            move.setAmount(Double.valueOf(txt.getText().toString()));
        } catch (NumberFormatException e) {
            return false;
        }

        spinner = (Spinner) findViewById(R.id.input_new_currency);
        if (spinner == null)
            return false;
        move.setCurrency(spinner.getSelectedItemPosition());

        DatePicker date = (DatePicker) findViewById(R.id.datePicker);
        if (date == null)
            return false;
        Calendar calendar = Calendar.getInstance();
        calendar.set(date.getYear(), date.getMonth(), date.getDayOfMonth());
        move.setWhen(calendar.getTime());

        // save
        FinanceDao dao = new FinanceDao(getApplicationContext());
        dao.open();
        long res = dao.addMove(move);
        dao.close();

        return (res != -1);
    }
}
