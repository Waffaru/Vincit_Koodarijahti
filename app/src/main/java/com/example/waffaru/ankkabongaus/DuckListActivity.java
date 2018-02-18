package com.example.waffaru.ankkabongaus;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Main activity of the app. Displays a list of sightings, and allows user to add a sighting.
 *
 * @author Gonzalo Ortiz
 * @version "%I%, %G%"
 * @since 1.8
 */
public class DuckListActivity extends AppCompatActivity {

    private JSONArray jArray;
    ListView listview;
    ArrayList<JSONObject> sightingList;
    SightingAdapter sightingAdapter;


    /**
     * Here is where we take the data containing our json and turn it into a listview.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duck_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_layout);
        try {
            jArray = new JSONArray(getIntent().getStringExtra("JSONString"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sightingList = new ArrayList<>();

        for(int i = 0; i < jArray.length(); i++) {
            try {
                adapter.add(jArray.get(i).toString());
                JSONObject obj = new JSONObject(jArray.get(i).toString());
                sightingList.add((JSONObject) jArray.get(i));
            } catch (JSONException e) {
            }
        }
        try {
            jArray.toJSONObject(jArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sightingAdapter = new SightingAdapter(sightingList, this);
        listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter(sightingAdapter);
    }

    /**
     * adds a new sighting to our list.
     *
     * This method starts a new dialog asking the user for input regarding the sighting,
     * and then sends it forward to an AsyncTask to send the new sighting to the user.
     * If user inputs erroneous or empty data, shows the user a Toast instead.
     *
     * @param view the view calling this method, in this case a Button.
     * @see SendData
     */
    public void addBird(View view) {
        //Building our dialogue here
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        Spinner speciesSpinner = (Spinner) mView.findViewById(R.id.spinnerSpecies);
        speciesSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, Species.getAsString()));
        EditText descriptionEdit = (EditText) mView.findViewById(R.id.dialogDescriptionEdit);
        EditText countEdit = (EditText) mView.findViewById(R.id.dialogCountEdit);
        Button cancelButton = (Button) mView.findViewById(R.id.dialogCancelButton);

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();

        // Listener for our cancel button, closes dialog.
        cancelButton.setOnClickListener(viewl -> {
            dialog.cancel();
        });
        Button confirmButton = (Button) mView.findViewById(R.id.dialogConfirmButton);
        // Listener for our cancel button, checks input and either shows Toast or sends data forward.
        confirmButton.setOnClickListener(view1 -> {
            String species = speciesSpinner.getSelectedItem().toString();
            String description = descriptionEdit.getText().toString();
            if(!species.isEmpty() && !countEdit.getText().toString().isEmpty()) {
                int count = Integer.parseInt(countEdit.getText().toString());
                for(int i = 0; i < Species.values().length; i++) {
                    if(Species.values()[i].getName().toLowerCase().equals(species.toLowerCase())) {
                        JsonObject jsonDuck = new JsonObject();
                        DateTime time = new DateTime();
                        DateTimeFormatter fmt = new DateTimeFormatterBuilder().append(ISODateTimeFormat.dateTimeNoMillis()).toFormatter().withOffsetParsed();
                        jsonDuck.addProperty("id", "");
                        jsonDuck.addProperty("species", species);
                        jsonDuck.addProperty("description", description);
                        jsonDuck.addProperty("dateTime", fmt.print(time));
                        jsonDuck.addProperty("count", count);
                        new SendData(jsonDuck).execute();
                        //dialog.cancel();
                    }
                }
            }
            else {
                Toast.makeText(this, "Please input correct values", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Extension of AsyncTask designed to send data to the server.
     */
    public class SendData extends AsyncTask<Void, Void, Void> {
        JsonObject jsonObject;
        boolean connectionSucceeded = false;
        String json;
        public SendData(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
            json = jsonObject.toString();
        }

        /**
         * opens a POST-connection with the server and sends a Json.
         * Also logs reply from server.
         *
         * @param voids
         * @return null
         */
        @Override
        protected Void doInBackground(Void... voids) {
            URL url = null;
            StringBuilder sb = null;
            String jsonResponse = null;
            try {
                //this url works only on a virtual android machine and only if server is run from
                url = new URL("http://10.0.2.2:8081/sightings/");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.addRequestProperty("Accept", "application/json");
                httpURLConnection.addRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestMethod("POST");

                OutputStreamWriter owr = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
                owr.write(jsonObject.toString());

                owr.close();

                int HttpResult = httpURLConnection.getResponseCode();
                if(HttpResult == HttpURLConnection.HTTP_OK) {
                    connectionSucceeded = true;
                }
                Log.d("HTTP Result:", String.valueOf(HttpResult));

                StringBuffer buffer = new StringBuffer();
                InputStream is = httpURLConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String inputLine;
                while((inputLine = reader.readLine()) != null) {
                    buffer.append(inputLine + "\n");
                    if (buffer.length() == 0) {
                        // Stream was empty. No point in parsing.
                        return null;
                    }
                }
                jsonResponse = buffer.toString();
                Log.d("Server response",jsonResponse);
                httpURLConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * passes our Json-object to updateListview-method.
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(connectionSucceeded) {
                try {
                    updateListView(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Adds our JSON-object to our sightingAdapter used to populate listview.
     * @param jsonObject the object to add.
     * @throws JSONException
     */
    public void updateListView(JsonObject jsonObject) throws JSONException {
        JSONObject json = new JSONObject(jsonObject.toString());
        sightingAdapter.addJSON(json);
    }

}
