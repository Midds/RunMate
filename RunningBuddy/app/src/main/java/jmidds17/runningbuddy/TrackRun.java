package jmidds17.runningbuddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TrackRun extends Activity implements OnMapReadyCallback {
    //Global Variables
    static LocationHelper mLoc;
    static boolean timer = false;

    static private GoogleMap customMap;
    private Marker currentLocMarker;
    static public String longitude = "-0.5431253";
    static public String latitude = "53.2260276";
    long startTime = 0;
    long stopTime = 0;
    long timePassed = 0;
    //String mLastUpdateTime = DateFormat.getDateTimeInstance().format(new Date());
    static int markerCount = 1;
    static PolylineOptions plannedRoute = new PolylineOptions();
    static Polyline polyline;
    static List<Marker> wayPoints = new ArrayList<Marker>();

    // Filename for saving route once user clicks save route button
    String filename = "savedroute";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("onCreate", "huh");
        // creating instance of locationhelper.
        mLoc = new LocationHelper(TrackRun.this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_run);

        // Getting a handle to the fragment where the map is located
        getMapFragmentHandle();

    }



    protected void onStart() {
        mLoc.mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mLoc.mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        // stop location updates when activity will go out of focus
        if (mLoc.mGoogleApiClient.isConnected()){
            mLoc.stopLocationUpdates();
        }
    }

    // This method will be called when onLocationChanged is called in LocationHelper class.
    // This means when the user location changes it updates the lat and long here so the users run
    // can therefore be tracked.
    static public void getUpdates(){
        // If the timer is not currently started it wont track any location changes.
        if (timer)
        {
            // Update location coordinates
            latitude = mLoc.getLatitude();
            longitude = mLoc.getLongitude();

            Log.e("new latitude", latitude);

            updateMap();
        }
    }

    public void startRunTimer(View view) {
        Chronometer ch1 = (Chronometer)findViewById(R.id.chronometer);
        startTime = SystemClock.elapsedRealtime();
        ch1.setBase(startTime);
        ch1.start();
        timer = true;

        // greying out the button once the timer is started
        Button startButton = (Button)findViewById(R.id.startRunButton);
        startButton.setEnabled(false);

        // enabling the finish run button
        Button finishButton = (Button)findViewById(R.id.finishRunButton);
        finishButton.setEnabled(true);
    }

    public void finishRunTimer(View view) {
        Chronometer ch1 = (Chronometer)findViewById(R.id.chronometer);
        ch1.stop();
        stopTime = ch1.getBase();
        timePassed = SystemClock.elapsedRealtime() - stopTime;
        Log.e("stop time = ", String.valueOf(stopTime));
        Log.e("time passed = ", String.valueOf(timePassed));
        timer = false;

        // greying out the button once the timer has ended
        Button finishButton = (Button)findViewById(R.id.finishRunButton);
        finishButton.setEnabled(false);

        // enabling the start run button again
        Button startButton = (Button)findViewById(R.id.startRunButton);
        startButton.setEnabled(true);

        // enabling the button to save the run after you have stopped the timer
        Button saveButton = (Button)findViewById(R.id.saveRunButton);
        saveButton.setEnabled(true);

    }

    // Using Location.distanceBetween to calculate the run distance. Retruns in kilometers.
    // Google (2016) Location [online]
    // Mountain View, California: Google. Available from
    // https://developer.android.com/reference/android/location/Location.html [Accessed 15 December 2016].
    public float calculateDistance(List<Marker> routeToMeasure){
        float distance = 0; // double to hold the final tallied distance
        float[] results = new float[routeToMeasure.size()]; // float array to hold the distances between each location

        // Getting distance between start point and first waypoint (because start point(current phone location) is not stored in 'wayPoints')
        Location.distanceBetween(Double.parseDouble(latitude), Double.parseDouble(longitude),
                routeToMeasure.get(0).getPosition().latitude, routeToMeasure.get(0).getPosition().longitude,
                results);

        // looping though each waypoint and adding the distance to result[] each time
        for (int i = 0; i < routeToMeasure.size() - 1; i++) {
            Location.distanceBetween(routeToMeasure.get(i).getPosition().latitude, routeToMeasure.get(i).getPosition().longitude,
                    routeToMeasure.get(i+1).getPosition().latitude, routeToMeasure.get(i+1).getPosition().longitude,
                    results);
        }

        // Tallying up results[] to get the final run distance
        for (float result : results) {
            distance = distance + result;
        }

        return distance;
    }

    // Saves the current markers as a route to a database and takes the user to SavedRoutes activity.
    public void saveRoute(View view) {
        // If the user hasn't added any waypoints then it won't save
        if (wayPoints.size() == 0)
        {
            // Show toast message that there is no waypoints to save
            Context context = getApplicationContext();
            CharSequence text = "You need at least one Way Point to save a route!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        // Else save waypoints
        else {
            // Temporary string to hold the marker lat/long coordinates - will hold cords for every marker on a new line for easier parsing later.
            // Defaulted to hold the user's current position.
            String tempLatLong = String.valueOf(latitude) + "," + String.valueOf(longitude) + "\n";
            // String to hold the default name of each route, will be configurable by user later
            String tempRouteName;

            DatabaseHelper mDbHelper = DatabaseHelper.getInstance(this);
            // Gets the data repository in write mode
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            // Sets the tempRouteName that will be used to write a default route name when the user adds a route.
            // This name will be number of records + 1. User can configure their own name later if they want.
            tempRouteName = "Route " + String.valueOf(DatabaseHelper.getNumRecords(db, DatabaseContract.SavedRoutesTable.TABLE_NAME) + 1);

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.SavedRoutesTable.COLUMN_NAME_1, tempRouteName);
            for (int i = 0; i < wayPoints.size(); i++) {
                tempLatLong = tempLatLong + String.valueOf(wayPoints.get(i).getPosition().latitude) + "," + String.valueOf(wayPoints.get(i).getPosition().longitude + "\n");
            }
            values.put(DatabaseContract.SavedRoutesTable.COLUMN_NAME_2, tempLatLong);
            values.put(DatabaseContract.SavedRoutesTable.COLUMN_NAME_3, String.valueOf(calculateDistance(wayPoints)));


            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(DatabaseContract.SavedRoutesTable.TABLE_NAME, null, values);


            db.close();

            // Finally start the intent to go to SavedRoutes activity
            Intent intent = new Intent(this, SavedRoutes.class);
            //start Activity
            startActivity(intent);
        }
    }


    // Gets called when app comes back into view eg after user has hit the home screen and returns to app screen.
    @Override
    public void onResume() {
        Log.e("TAG", "onResume ");
        super.onResume();

        //markerCount = 1; // resets marker count
        //wayPoints.clear(); // clears the listarray of waypoints
        // customMap.clear(); // removes all custom markers from map
        //plannedRoute = new PolylineOptions(); // Clears the polyline route on reset
        // updateUI(); // replaces currentlocation origin marker on map
        //removeLastMarker();

        if (latitude != null)
        {
            Log.e("onResume", String.valueOf(latitude));
        }
        else
        {
            Log.e("onResume planroute2", "latitude null");
        }

        // Getting new location coordinates (before configuring map with these coordinates)
        new AsyncTaskGetLocation().execute();

    }


    /*public void removeLastMarker(View view){
        markerCount = markerCount - 1; // removes one marker from count
        Log.e("TAG", String.valueOf(wayPoints.size()));
        wayPoints.remove(wayPoints.size() - 1); // removes the last marker
        Log.e("TAG", String.valueOf(wayPoints.size()));

        for (int i = 0; i < wayPoints.size() - 1; i++){
            tempRoute.add(wayPoints.get(i).getPosition())
                    .width(5);
        }

        customMap.clear(); // removes all custom markers from map


        plannedRoute = new PolylineOptions(); // Clears the polyline route on reset
        updateUI(); // replaces currentlocation origin marker on map


        polyline = customMap.addPolyline(tempRoute);
    }*/

    static private void updateMap(){
        // Adding marker to the map
        // This creates a marker but so it can be saved in the wayPoints list and used later, but doesn't
        // show the marker on the map - as it can get too clustered with so many markers.
        Marker wayPointM = customMap.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .visible(false));

        // adding points to the polyline where the new marker is
        plannedRoute.add(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                .width(5);

        // adding the polyline to the map
        polyline = customMap.addPolyline(plannedRoute);

        // updating waypoints (adding marker to list of markers)
        wayPoints.add(wayPointM);

        customMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 15));

        markerCount++;
    }

    public void onMapReady(GoogleMap map) {
        Log.e("onMapReady", map.toString());

        customMap = map; // using global variable customMap so it can be changed in other scopes

        // When map is clicked place a marker
        // Rathore, A. (2013) Add Marker on Android Google Map via touch or tap.
        // [stack overflow] 12 September. Available from
        // https://stackoverflow.com/questions/17143129/add-marker-on-android-google-map-via-touch-or-tap [Accessed 27 November 2016].
        customMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            // Will run when the map is tapped on - this is used to add markers to plot a route
            @Override
            public void onMapClick(LatLng point) {
                // Adding marker to the map
                // This creates a marker but so it can be saved in the wayPoints list and used later, but doesn't
                // show the marker on the map - as it can get too clustered with so many markers.
                Marker wayPointM = customMap.addMarker(new MarkerOptions()
                        .position(new LatLng(point.latitude, point.longitude))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .visible(false));

                // adding points to the polyline where the new marker is
                plannedRoute.add(new LatLng(point.latitude, point.longitude))
                        .width(5);

                // adding the polyline to the map
                polyline = customMap.addPolyline(plannedRoute);

                // updating waypoints (adding marker to list of markers)
                wayPoints.add(wayPointM);

                markerCount++;
            }
        });
    }

    // Controls what the user first sees on the map (default location, zoom, markers)
    private void configureMapDefault() {
        customMap.clear(); // Clears current marker before adding an updated one
        Log.e("TAG", latitude);
        if (latitude != null) {
            // Google (2016) CameraUpdateFactory [online]
            // Mountain View, California: Google. Available from
            // https://developers.google.com/android/reference/com/google/android/gms/maps/CameraUpdateFactory [Accessed 27 November 2016].
            customMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 15) );
            currentLocMarker = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                    .title("You are here"));
            plannedRoute.add(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)));
        } else {
            // If no location exists, defaults to 0,0 so app doesn't crash
            currentLocMarker = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .title("Current Location Unknown"));
        }
    }



    private void getMapFragmentHandle(){
        // Google (2016) Map Objects [online]
        // Mountain View, California: Google. Available from
        // https://developers.google.com/maps/documentation/android-api/map [Accessed 27 November 2016].
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.trackMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plan_route2, menu);
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


    public class AsyncTaskGetLocation extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            Log.e("onPreExecute", "huh");
            pd=ProgressDialog.show(TrackRun.this,"","Please Wait",false);
        }

        @Override
        // This method will get the last long/lat from the LocationHelper class to append to the api call URL.
        // After this the call will be made using the httpConnect class, and the returned JSON will be parsed
        // and weatherValues will be changed to reflect this.
        protected String doInBackground(String... arg0)  {
            try {
                Log.e("doInBackground ", "planRoute2 huh");



                while (mLoc.mGoogleApiClient.isConnecting())
                {
                    // Log.e("doInBackground ", "its connecting");
                    publishProgress();
                    if (mLoc.mGoogleApiClient.isConnected())
                    {
                        Log.e("doInBackground ", "ITS DONE JIM!");
                        break;
                    }
                }

                String latitudea = mLoc.getLatitude();
                String longitudea = mLoc.getLongitude();


                if(latitudea != null)
                {
                    Log.e("doInBackground jim! ", String.valueOf(latitude));
                }
                else
                {
                    Log.e("doInBackground", "its null jim");

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate()
        {

        }

        @Override
        // Below method will run when service HTTP request is complete, this will stop location updates
        // from LocationHelper, as well as setting the new information to their text views.
        protected void onPostExecute(String strFromDoInBg) {
            Log.e("onPostExecute", "huh");
            //mLoc.stopLocationUpdates();
            // updating the text views on the app with new info
            latitude = mLoc.getLatitude();
            longitude = mLoc.getLongitude();
            if(latitude != null)
            {
                Log.e("onPostExecute", latitude);
            }

            // Configure the map
            configureMapDefault();
            pd.dismiss();
        }
    }

    public class AsyncTaskStartTimer extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            Log.e("onPreExecute", "huh");
            pd=ProgressDialog.show(TrackRun.this,"","Please Wait",false);
        }

        @Override

        protected String doInBackground(String... arg0)  {
            try {


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate()
        {

        }

        @Override
        // Below method will run when service HTTP request is complete, this will stop location updates
        // from LocationHelper, as well as setting the new information to their text views.
        protected void onPostExecute(String strFromDoInBg) {
            Log.e("onPostExecute", "huh");

            configureMapDefault();
        }
    }
}
