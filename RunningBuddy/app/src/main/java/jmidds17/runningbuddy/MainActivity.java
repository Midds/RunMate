package jmidds17.runningbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
// add below
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import android.widget.*;
import java.util.Date;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // global variables for the weather widget
    String weatherLocation;
    String weatherTemperature;
    String weatherDescription;
    String weatherWind;
    String weatherIcon;
    static Bitmap bitmap;
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;

    // filename for saving location data
    String filename = "latestAPIRequest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        checkWeather();
    }

    public void onConnected(Bundle connectionHint) {
        // getting last know location on the phone - not a new location
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        Log.e("TAG", String.valueOf(mLastLocation.getLatitude()));

        // Google (2016) Receiving Location Updates: Request Location Updates [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/location/receive-location-updates.html [Accessed 23 November 2016].

        // below If refers to a boolean flag that is used to track whether user has turned
        // location updates on or off
        // for now i will assume this is true and startLocationUpdates
        //if (mRequestingLocationUpdates) {
        //    startLocationUpdates();
        //}
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // Gets called when app comes back into view eg after user has hit the home screen and returns to app screen.
    @Override
    public void onResume() {
        Log.e("TAG", "onResume ");
        super.onResume();
        checkWeather();
    }

    // called when user touches the Plan Route button
    public void startRoutePlan(View view) {
        // intent to start PlanRoute
        Intent intent = new Intent(this, PlanRoute.class);
        // start Activity
        startActivity(intent);
    }

    // called when user touches the Saved routes button
    public void showRoutes(View view) {
        //  intent to start SavedRoutes
        Intent intent = new Intent(this, SavedRoutes.class);
        // start Activity
        startActivity(intent);
    }

    public void configureTrack(View view) {
        // create an intent to start TrackRun
        Intent intent = new Intent(this, TrackRun.class);
        // start Activity
        startActivity(intent);
    }

    // Called in oncreate and also in onresume
    private void checkWeather (){
        try {
            // checking whether phone is connected to the internet before trying to use api / call async class
            // Google (2016) Determining and Monitoring the Connectivity Status [online]
            // Mountain View, California: Google. Available from
            // https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html [Accessed 14 December 2016].
            ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            // When bool == true you have connection and can make api request
            if (isConnected) {
                new AsyncTaskParseJson().execute();
            }
            // If !bool then there's no internet connection - so try and use the most recent data saved.
            if (!isConnected) {
                getMostRecent();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public class AsyncTaskParseJson extends AsyncTask<String, String, String> {

        // set the url of the web service to call
        String yourServiceUrl = "http://api.openweathermap.org/data/2.5/weather?lat=53.2260276&lon=-0.5431253&units=metric&APPID=9394674264a196a20ada133ea74bc768";

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
                    // Parsing json objects and arrays to pull the needed values.
                    // Json returns a main object, but contains other objects and arrays within itself so these need to be parsed to pull meaningful data.
                    weatherLocation = jObject.getString("name"); // Current location is returned as a string, in the main object.
                    // converting weather temp to celsius (it is returned as kelvin by api)
                    weatherTemperature = String.valueOf((int) jObject.getJSONObject("main").getDouble("temp")); // Turning double to int to remove the decimal places in the temperature
                    weatherDescription = jObject.getJSONArray("weather").getJSONObject(0).getString("description"); // Weather description is stored in a string, in an object, within an array, within the main object.
                    weatherWind = String.valueOf(jObject.getJSONObject("wind").getDouble("speed")); // Wind speed stored in a double, within an object, within the main object.
                    weatherIcon = jObject.getJSONArray("weather").getJSONObject(0).getString("icon");

                    String imageurl = "http://openweathermap.org/img/w/" + weatherIcon +".png";
                    // parse the cover art image url to proper URL type
                    URL u = new URL(imageurl);
                    // download image cover art from url and save as a bitmap
                    InputStream is = u.openConnection().getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);

                    // Saving wanted values to file for later use, each value on a new line for easier parsing later on
                    saveLatestRequest(weatherLocation + "\n" + String.valueOf(weatherTemperature) + "\n" + weatherDescription + "\n" + String.valueOf(weatherWind));
                    //saveLatestRequest(json);
                    // updating the text views on the app with new info
                    setWeatherWidget();
                }
                // If call from httpconnect returns null. Try and access the last file saved.
                if (json == null) {
                    getMostRecent();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        // below method will run when service HTTP request is complete, will then bind tweet text in arrayList to ListView
        protected void onPostExecute(String strFromDoInBg) {
            setWeatherWidget();
        }
    }


    private void parseJson (){

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

    public void getMostRecent()
    {
        // create new stringbuilder and read text file line by line
        StringBuilder datax = new StringBuilder("");
        try {
            // open location file from private storage area
            FileInputStream fIn = openFileInput(filename);
            InputStreamReader isr = new InputStreamReader( fIn ) ;
            BufferedReader buffreader = new BufferedReader( isr ) ;
            String tempWeatherStrings[] = new String[4];
            // loop to read each line, each line holds a different variable for the weather widget
            for (int i = 0; i < tempWeatherStrings.length; i++)
            {
                // Appending each line in the saved file to this string array
                tempWeatherStrings[i] = buffreader.readLine();
            }

            // setting the weather widget values to the values from the saved file
            weatherLocation = tempWeatherStrings[0];
            weatherTemperature = tempWeatherStrings[1];
            weatherDescription = tempWeatherStrings[2];
            weatherWind = tempWeatherStrings[3];

            // updating the text views on the app with new info
            setWeatherWidget();

        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }
    }

    // Updating the data on the weather widget's text views
    public void setWeatherWidget() {
        TextView tv1 = (TextView)findViewById(R.id.weatherTemperature);
        tv1.setText(String.valueOf(weatherTemperature) +  (char) 0x00B0 + "C");

        TextView tv2 = (TextView)findViewById(R.id.weatherLocation);
        tv2.setText(weatherLocation);

        TextView tv3 = (TextView)findViewById(R.id.weatherDescription);
        tv3.setText(weatherDescription);

        TextView tv4 = (TextView)findViewById(R.id.weatherWind);
        tv4.setText("Wind = " + String.valueOf(weatherWind) + "mph");

        ImageView iv1 = (ImageView) findViewById(R.id.weatherIcon);
        iv1.setImageBitmap(bitmap);
    }
}
