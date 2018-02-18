package com.example.waffaru.ankkabongaus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Adapter to be used with ListView. takes a JSONObject-list as input.
 * @author Gonzalo Ortiz
 * @version "%I%, %G%"
 * @since 1.8
 *
 */

public class SightingAdapter extends BaseAdapter {
    List<JSONObject> sightings;
    Context context;

    public SightingAdapter(List<JSONObject> sightings, Context context) {
        super();

        this.sightings = sightings;
        this.context = context;
        sort();
    }

    @Override
    public int getCount() {
        return sightings.size();
    }

    @Override
    public Object getItem(int i) {
        return sightings.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Override method of getView to populate views for each row in our ListView.
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater != null ? inflater.inflate(R.layout.list_layout, parent, false) : null;
        TextView duckName = (TextView) rowView.findViewById(R.id.name);
        TextView date = (TextView) rowView.findViewById(R.id.date);
        try {
            duckName.setText(sightings.get(position).getString("species"));
            date.setText(sightings.get(position).getString("dateTime"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AlertDialog dialog = null;
        rowView.setOnClickListener(view -> {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
            View mView = inflater.inflate(R.layout.sighting_expanded, null);
            TextView nameView = (TextView) mView.findViewById(R.id.nameText);
            TextView dateView = (TextView) mView.findViewById(R.id.dateText);
            TextView descriptionView = (TextView) mView.findViewById(R.id.descriptionText);
            Button cancelButton = (Button) mView.findViewById(R.id.cancelButton);
            try {
                nameView.setText(sightings.get(position).getString("species"));
                dateView.setText(sightings.get(position).getString("dateTime"));
                descriptionView.setText(sightings.get(position).getString("description"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mBuilder.setView(mView);
            AlertDialog dialog1 = mBuilder.create();
            dialog1.show();
            cancelButton.setOnClickListener(viewl -> {
                dialog1.cancel();
            });
        });
        return rowView;
    }

    /**
     * Adds a JSONObject to the list.
     * @param object
     */
    public void addJSON(@Nullable JSONObject object) {
        this.sightings.add(object);
        sort();
        notifyDataSetChanged();
    }

    /**
     * Sorts our JSONObject-list based on date.
     */
    private void sort() {
        this.sightings.sort((jsonObject, t1) -> {
            DateTime time1 = null;
            DateTime time2 = null;
            try {
                time1 = new DateTime(jsonObject.getString("dateTime"));
                time2 = new DateTime(t1.getString("dateTime"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (time1.getMillis() > time2.getMillis()) {
                return -1;

            } else if(time1.getMillis() < time2.getMillis()) {
                return 1;
            }
            else {
                return 0;
            }
        });
    }


}
