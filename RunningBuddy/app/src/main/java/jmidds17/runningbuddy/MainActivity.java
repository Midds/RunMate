package jmidds17.runningbuddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import org.json.JSONObject;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import android.widget.*;

public class MainActivity extends Activity {
    // global variables for the weather widget
    private String weatherLocation;
    private String weatherTemperature;
    private String weatherDescription;
    private String weatherWind;
    private String weatherIcon;
    static Bitmap bitmap;
    // filename for saving location data
    String filename = "latestAPIRequest";
    LocationHelper mLoc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Gets called when app comes back into view eg after user has hit the home screen and returns to app screen.
    @Override
    public void onResume() {
        super.onResume();

        try {
            // Creating instance of LocationHelper.
            // This will start the connection to the googleApiClient in LocationHelper class so that if the user
            // presses the check weather button it will be already connected and ready to get location
            mLoc = new LocationHelper(MainActivity.this);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // stop location updates when activity goes out of focus
        if (mLoc.mGoogleApiClient.isConnected()){
            mLoc.stopLocationUpdates();
        }
    }

    // called when user touches the Plan Route button
    public void startRoutePlan2(View view) {
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

    // called when user clicks the Saved routes button
    public void startTrackRun(View view) {
        //  intent to start SavedRoutes
        Intent intent = new Intent(this, TrackRun.class);
        // start Activity
        startActivity(intent);
    }

    // called when user clicks the check weather button
    public void configureWeather(View view) {
        // making sure google api client is connected before performing checkweather()
        if (!mLoc.mGoogleApiClient.isConnected())
        {
            mLoc.connectToApi();
            mLoc.startLocationUpdates();
        }
        checkWeather();
    }


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

        boolean alertFlag = false;
        ProgressDialog pd;
        // Set the url of the web service to call. This will be used as default url if LocationHelper
        // cannot get an updated location to use for whatever reason such as gps not enabled. This is to ensure that the app doesn't crash.
        String yourServiceUrl = "http://api.openweathermap.org/data/2.5/weather?lat=53.2260276&lon=-0.5431253&units=metric&APPID=9394674264a196a20ada133ea74bc768";

        // This method is used for preparing the locationhelper class. It creates an instance of the
        // class before calling the getLatitude and getLongitude methods in "doInBackground"
        // This is needed to give the LocationHelper time to connect the GoogleApi. If these methods
        // are called at the same time than this is, then they will return null.
        @Override
        protected void onPreExecute() {
            // letting the user know something is happening if it's taking too long
            pd=ProgressDialog.show(MainActivity.this,"","Please Wait",false);
        }

        @Override
        // This method will get the last long/lat from the LocationHelper class to append to the api call URL.
        // After this the call will be made using the httpConnect class, and the returned JSON will be parsed
        // and weatherValues will be changed to reflect this.
        protected String doInBackground(String... arg0)  {
            try {
                String latitude = mLoc.getLatitude();
                String longitude = mLoc.getLongitude();

                // Changes the long and lat in the serviceURL to lat known long/lat
                if (latitude != null && longitude != null){
                    yourServiceUrl = "http://api.openweathermap.org/data/2.5/weather?lat=" +
                            latitude + "&lon=" + longitude +
                            "&units=metric&APPID=9394674264a196a20ada133ea74bc768";
                }
                else if (latitude == null)
                {
                    // At this point if latitude is null it cannot be an internet problem as the app tested for a connection before this in checkWeather()
                    // This means it must be a GPS issue - i.e location is not active on the phone or there is no gps signal.
                    // Therefore alertFlag is set to true. On postExecute if alertFlag is true it will call the alertDialog class.
                    // This has to happen in postExecute as trying to interact with the UI on a background thread gives a java.lang.RuntimeException.
                    alertFlag = true;
                }

                // create new instance of the httpConnect class
                httpConnect jParser = new httpConnect();
                // get json string from service url
                String json = jParser.getJSONFromUrl(yourServiceUrl);
                JSONObject jObject = new JSONObject(json);

                // Saves the json string to file if it isn't null. This can then be used later to give
                // data even if no internet connection or the api call limit is reached.
                if (json != null) {
                    // Parsing json objects and arrays to pull the needed values.
                    // Json returns a main object, but contains other objects and arrays within itself so these need to be parsed to pull meaningful data.
                    weatherLocation = jObject.getString("name"); // Current location is returned as a string, in the main object.
                    // converting weather temp to celsius (it is returned as kelvin by api)
                    weatherTemperature = String.valueOf((int) jObject.getJSONObject("main").getDouble("temp")); // Turning double to int to remove the decimal places in the temperature
                    weatherDescription = jObject.getJSONArray("weather").getJSONObject(0).getString("description"); // Weather description is stored in a string, in an object, within an array, within the main object.
                    weatherDescription = weatherDescription.substring(0, 1).toUpperCase() + weatherDescription.substring(1);
                    weatherWind = String.valueOf(jObject.getJSONObject("wind").getDouble("speed")); // Wind speed stored in a double, within an object, within the main object.
                    weatherIcon = jObject.getJSONArray("weather").getJSONObject(0).getString("icon");

                    // the api i'm using gives a code to a weather icon in the returned json, this code can then be used in a URL to fetch the icon from their website
                    String imageurl = "http://openweathermap.org/img/w/" + weatherIcon +".png";
                    // parse the cover art image url to proper URL type
                    URL u = new URL(imageurl);
                    // download weather icon from url and save as a bitmap
                    InputStream is = u.openConnection().getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);

                    // Saving wanted values to file for later use, each value on a new line for easier parsing later on
                    saveLatestRequest(weatherLocation + "\n" + String.valueOf(weatherTemperature) + "\n" + weatherDescription + "\n" + String.valueOf(weatherWind));
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
        // Below method will run when service HTTP request is complete, this will stop location updates
        // from LocationHelper, as well as setting the new information to their text views.
        protected void onPostExecute(String strFromDoInBg) {
            // updating the text views on the app with new info
            setWeatherWidget();
            pd.dismiss();
            if (alertFlag) {
                // call showAlertDialog
                CallAlertDialog.alert(MainActivity.this);
            }
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

    // writing to file
    private void saveLatestRequest (String toSave)
    {
        // Creating a file in local storage to save to
        FileOutputStream outputStream;
        File file = getFileStreamPath(filename);

        // No need to check if file exists because it only needs to save the most up to date http request
        // so it's ok if it overwrites a previous file.
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

        }
        catch (FileNotFoundException fE){
            // This will occur if the user has no backup data saved in file (i.e its the first time they run AND have no internet)
            // This is ok and it won't crash the app but a toast to tell the user they aren't connected is still used.
            fE.printStackTrace();

            // Show toast message that there are no waypoints to save
            Context context = getApplicationContext();
            CharSequence text = "No Internet connection detected.\nPlease connect and try again.";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    // Updating the data on the weather widget's and making views visable
    public void setWeatherWidget() {
        TextView tv1 = (TextView)findViewById(R.id.weatherTemperature);
        tv1.setText(String.valueOf(weatherTemperature) + (char) 0x00B0 + "C");
        tv1.setVisibility(View.VISIBLE);

        // displaying some helpful flavour text depending on the temperature
        double tempTemperature = Double.parseDouble(weatherTemperature);
        if (tempTemperature <= 0){
            TextView tv5 = (TextView)findViewById(R.id.weatherFlavourText);
            tv5.setText("It's freezing! \nMake sure to wrap up warm if you're heading out.");
        }
        else if (tempTemperature > 0 && tempTemperature < 6){
            TextView tv5 = (TextView)findViewById(R.id.weatherFlavourText);
            tv5.setText("It's cold! \nYou might want a few layers on if you're heading out.");
        }
        else if (tempTemperature > 5 && tempTemperature < 11){
            TextView tv5 = (TextView)findViewById(R.id.weatherFlavourText);
            tv5.setText("It's a bit chilly! \nYou might want to put some layers on if you're heading out.");
        }
        else if (tempTemperature > 10 && tempTemperature < 21){
            TextView tv5 = (TextView)findViewById(R.id.weatherFlavourText);
            tv5.setText("Looks like the perfect temperature for a run!");
        }
        else if (tempTemperature > 20 && tempTemperature < 28){
            TextView tv5 = (TextView)findViewById(R.id.weatherFlavourText);
            tv5.setText("It's hot out! \nMake sure you are drinking plenty of water.");
        }
        else if (tempTemperature > 27){
            TextView tv5 = (TextView)findViewById(R.id.weatherFlavourText);
            tv5.setText("It's scorching! \nYou might want to wait for it to get a bit cooler.");
        }

        // making the frame that the flavour text is in visible
        findViewById(R.id.weatherFlavourFrame).setVisibility(View.VISIBLE);

        // weather location
        TextView tv2 = (TextView)findViewById(R.id.weatherLocation);
        tv2.setText(weatherLocation);
        tv2.setVisibility(View.VISIBLE);

        // weather description
        TextView tv3 = (TextView)findViewById(R.id.weatherDescription);
        tv3.setText(weatherDescription);
        tv3.setVisibility(View.VISIBLE);

        // weather wind speed
        TextView tv4 = (TextView)findViewById(R.id.weatherWind);
        tv4.setText("Wind = " + String.valueOf(weatherWind) + "mph");
        tv4.setVisibility(View.VISIBLE);

        // weather icon
        ImageView iv1 = (ImageView) findViewById(R.id.weatherIcon);
        iv1.setImageBitmap(bitmap);
        iv1.setVisibility(View.VISIBLE);

        // making the running man icon invsible
        ImageView iv2 = (ImageView)findViewById(R.id.runnerIcon);
        iv2.setVisibility(View.INVISIBLE);
    }
}
