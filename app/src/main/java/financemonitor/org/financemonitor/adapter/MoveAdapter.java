package financemonitor.org.financemonitor.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import financemonitor.org.financemonitor.R;
import financemonitor.org.financemonitor.dao.MoveEntry;

public class MoveAdapter extends ArrayAdapter<MoveEntry> {

    private Activity context;
    private List<MoveEntry> moves;

    public MoveAdapter(Activity context, int textViewResourceId, List<MoveEntry> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        moves = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) { //reuse view
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.item_list, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.amount = (TextView) view.findViewById(R.id.txt_item_amount);
            viewHolder.when = (TextView) view.findViewById(R.id.txt_item_when);
            viewHolder.category = (TextView) view.findViewById(R.id.txt_item_category);
            viewHolder.id = (TextView) view.findViewById(R.id.txt_item_id);
            view.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        String currency;
        switch(moves.get(position).getCurrency()) {
            case 0:
                currency = "€";
                break;
            case 1:
                currency = "$";
                break;
            case 2:
            default:
                currency = "£";
                break;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        String str = currency + " " + df.format(moves.get(position).getAmount());
        holder.amount.setText(str);
        holder.when.setText(moves.get(position).getWhen().toString());
        String tmp =  moves.get(position).getCategory().getCategory() + " - "  +
                                                    moves.get(position).getCategory().getEntry();
        holder.category.setText(tmp);
        holder.id.setText(String.valueOf(moves.get(position).getId()));
        return view;
    }

    static public class ViewHolder {
        public TextView amount;
        public TextView when;
        public TextView category;
        public TextView id;
    }

}
