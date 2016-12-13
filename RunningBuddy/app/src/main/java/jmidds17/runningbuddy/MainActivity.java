package jmidds17.runningbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
// add below
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import android.widget.*;
import java.util.Date;
import android.util.Log;

public class MainActivity extends Activity {
    String weatherData;
    String weatherLocation;
    int weatherTemperature;
    String weatherDescription;
    double weatherWind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // weather api key - 9394674264a196a20ada133ea74bc768
        // need to check whether its connect to the internet before tryin to use api / call an async class
        checkWeather();
        Log.d("TAG", "on create");

    }

    /** Called when user touches the CLICK ME! button */
    public void startRoutePlan(View view) {

        // create an intent to start the activity called MainActivity
        Intent intent = new Intent(this, PlanRoute.class);
        // start Activity
        startActivity(intent);
    }

    public void showRoutes(View view) {

        // create an intent to start the activity called MainActivity
        Intent intent = new Intent(this, SavedRoutes.class);
        // start Activity
        startActivity(intent);
    }

    public void configureTrack(View view) {
        // create an intent to start the activity called MainActivity
        Intent intent = new Intent(this, TrackRun.class);
        // start Activity
        startActivity(intent);
    }

    private void checkWeather (){
        new AsyncTaskParseJson().execute();
        Log.d("TAG", "checkweather");

    }


    // added asynctask class methods below -  you can make this class as a separate class file
    public class AsyncTaskParseJson extends AsyncTask<String, String, String> {

        // set the url of the web service to call
        String yourServiceUrl = "http://api.openweathermap.org/data/2.5/weather?lat=53.2260276&lon=-0.5431253&APPID=9394674264a196a20ada133ea74bc768";

        @Override
        // this method is used for......................
        protected void onPreExecute() {}

        @Override
        // this method is used for...................
        protected String doInBackground(String... arg0)  {

            try {
                // create new instance of the httpConnect class
                httpConnect jParser = new httpConnect();

                // get json string from service url
                String json = jParser.getJSONFromUrl(yourServiceUrl);

                JSONObject jObject = new JSONObject(json);
                // save returned json to your test string
                // weatherData = jObject.getString("temp");

                // Saves the json string to file if it isn't null. This can then be used later to give
                // data even if no internet connection or the api call limit is reached.
                if (json != null) {
                    saveLatestRequest(json); // Saving json to file for later use

                    // Parsing json objects and arrays to pull the needed values.
                    // Json returns a main object, but contains other objects and arrays within itself so these need to be parsed to pull meaningful data.
                    weatherLocation = jObject.getString("name"); // Current location is returned in the default object.
                    // converting weather temp to celsius (it is returned as kelvin by api)
                    double weatherTemperatureTemp = (jObject.getJSONObject("main").getDouble("temp")) - 273.15; // Temperature is stored in a double within another object.
                    weatherTemperature = (int)weatherTemperatureTemp;
                    weatherDescription = jObject.getJSONArray("weather").getJSONObject(0).getString("description"); // Weather description is stored in a string in an object within an array within the main object.
                    weatherWind = jObject.getJSONObject("wind").getDouble("speed"); // Wind speed stored in a double within an object, within the main object.

                }
                // If call from httpconnect returns null. Try and access the last file saved.
                if (json == null) {
                    json = getMostRecent();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        // below method will run when service HTTP request is complete, will then bind tweet text in arrayList to ListView
        protected void onPostExecute(String strFromDoInBg) {
            TextView tv1 = (TextView)findViewById(R.id.weatherTemperature);
            tv1.setText(String.valueOf(weatherTemperature) +  (char) 0x00B0 + "C");

            TextView tv2 = (TextView)findViewById(R.id.weatherLocation);
            tv2.setText(weatherLocation);

            TextView tv3 = (TextView)findViewById(R.id.weatherDescription);
            tv3.setText(weatherDescription);

            TextView tv4 = (TextView)findViewById(R.id.weatherWind);
            tv4.setText("Wind = " + String.valueOf(weatherWind) + "mph");

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveLatestRequest (String toSave)
    {
        String filename = "latestAPIRequest";
        // Creating a file in local storage to save to
        FileOutputStream outputStream;
        File file = getFileStreamPath(filename);

        // No need to check if file exists because it only needs to save the most up to date http request
        // so it's ok if it overwrites a previous file.
        Log.d("File saving here", "saveLatestRequest");
        try {
            outputStream = openFileOutput(filename, MODE_PRIVATE);
            outputStream.write(toSave.getBytes());
            outputStream.write("\r\n".getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMostRecent()
    {
        return "test";
    }
}
