package com.example.waffaru.ankkabongaus;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *The main class of the program. Fetches data from the server and sends it forward to the next
 * activity.
 *
 * @author Gonzalo Ortiz
 * @version "%I%, %G%"
 * @since 1.8
 *
 */
public class MainActivity extends AppCompatActivity implements MyCallback {
    Button click;
    TextView dataView;
    ProgressBar bar;

    /**
     * Initializing attributes with views from our activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        click = (Button) findViewById(R.id.button);
        dataView = (TextView) findViewById(R.id.data);
        bar = (ProgressBar) findViewById(R.id.progressBar);
    }

    /**
     * fired when user clicks on a corresponding button
     *
     * The method initializes a new FetchData-object that extends AsyncTask, and executes it.
     *
     * @param view the view firing the method
     * @see FetchData
     */
    public void fetchData(View view) {
        MainActivity.FetchData fetcher = new MainActivity.FetchData();
        fetcher.execute();
    }

    /**
     * Interface method that we're using to start a new activity using values passed
     * from {@link FetchData}
     *
     * Is fired from {@link FetchData#onPostExecute(Void)}, essentially makin sure our AsyncTask
     * has finished doing what it's doing before passing a String containing JSON forward,
     * making sure we don't pass null-values. When this is done, it creates a new intent and passes
     * the JSON to the new intent. Then it starts a new activity using this intent.
     *
     * @param s the JSON-string received from {@link FetchData#onPostExecute(Void)}
     */
    @Override
    public void sendToActivity(String s) {
        Intent i = new Intent(this, DuckListActivity.class);
        i.putExtra("JSONString", s);
        startActivity(i);
    }

    /**
     * AsyncTask extension that we're using to handle the data fetching from the server.
     */
    private class FetchData extends AsyncTask<Void, Void, Void > {
        String dataParsed;
        String singleParsed;
        String data;

        /**
         * Fetches data from the server in the background and stores it in a class-variable.
         * @param voids
         * @return null
         */
        @Override
        protected Void doInBackground(Void... voids) {
            data = "";
            dataParsed ="";
            singleParsed = "";
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                //this url works only on a virtual android machine and only if server is run from
                URL url = new URL("http://10.0.2.2:8081/sightings");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while(line != null) {
                    line = bufferedReader.readLine();
                    if(line != null) {
                        data = data + line;
                    }
                }
                JSONArray jArray = new JSONArray(data);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Displays information to the user while data fetches.
         */
        @Override
        protected void onPreExecute() {
            bar.setVisibility(View.VISIBLE);
            dataView.setVisibility(View.VISIBLE);
        }

        /**
         * Hides loading info shown to user and sends out data to callback function
         * sendToactivity()
         *
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            sendToActivity(data);
            bar.setVisibility(View.GONE);
            dataView.setVisibility(View.GONE);
        }
    }



}
