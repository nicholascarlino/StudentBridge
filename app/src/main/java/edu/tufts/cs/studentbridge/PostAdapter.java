package edu.tufts.cs.studentbridge;

/**
 * Created by George on 12/2/2016.
 *
 * Custom ListAdapter that will contain PostItems
 */

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PostAdapter extends ArrayAdapter<PostItem> {

    private final Context context;
    private final ArrayList<PostItem> itemsArrayList;

    public PostAdapter(Context context, ArrayList<PostItem> itemsArrayList) {

        super(context, R.layout.post_item, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    //Gets the values and shows the correct data
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Get rowView from inflater
        View rowView = inflater.inflate(R.layout.post_item, parent, false);

        //Get the two text view from the rowView
        TextView textView = (TextView) rowView.findViewById(R.id.text);
        TextView userView = (TextView) rowView.findViewById(R.id.user);
        TextView timeView = (TextView) rowView.findViewById(R.id.time);

        //Set the text for textView
        textView.setText(itemsArrayList.get(position).get_text());
        userView.setText(itemsArrayList.get(position).get_user());
        timeView.setText(itemsArrayList.get(position).get_time());

        //Return rowView
        return rowView;
    }
}