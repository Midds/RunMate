package jmidds17.runningbuddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class PlanRoute extends Activity implements OnMapReadyCallback {
    // Global Variables needed for displaying map and location
    LocationHelper mLoc;
    private GoogleMap customMap;
    public String longitude = "-0.5431253";
    static String latitude = "53.2260276"; // Default to lincoln just in case
    int markerCount = 1;
    PolylineOptions plannedRoute = new PolylineOptions();
    Polyline polyline;
    List<Marker> wayPoints = new ArrayList<Marker>();

    // Global variables needed for saving route to database
    SQLiteDatabase db;
    DatabaseHelper mDbHelper;
    String tempLatLong;
    String tempRouteName;
    double tempDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("onCreate", "huh");
        // creating instance of locationhelper.
        mLoc = new LocationHelper(PlanRoute.this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_route);

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
    protected void onPause() {
        super.onPause();
        // stop location updates when activity will go out of focus
        if (mLoc.mGoogleApiClient.isConnected()){
            mLoc.stopLocationUpdates();
        }
    }


    public void textChangeButton(View view) {
        mLoc = new LocationHelper(PlanRoute.this);

        latitude = mLoc.getLatitude();
        longitude = mLoc.getLongitude();

        configureMapDefault();
    }

    // Gets called when app comes back into view eg after user has hit the home screen and returns to app screen.
    @Override
    public void onResume() {
        Log.e("TAG", "onResume ");
        super.onResume();


        // Getting new location coordinates (before configuring map with these coordinates)
        new AsyncTaskGetLocation().execute();

    }

    // Resets the map
    public void removeLastMarker(View view){
        markerCount = 1; // resets marker count
        wayPoints.clear(); // clears the listarray of waypoints
        customMap.clear(); // removes all custom markers from map
        plannedRoute = new PolylineOptions(); // Clears the polyline route on reset
        updateUI(); // replaces currentlocation origin marker on map
    }

    private void updateUI() {
        configureMapDefault();
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
        if (latitude != null) {
            // Google (2016) CameraUpdateFactory [online]
            // Mountain View, California: Google. Available from
            // https://developers.google.com/android/reference/com/google/android/gms/maps/CameraUpdateFactory [Accessed 27 November 2016].
            customMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 15));
            Marker startPosition = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                    .title("You are here"));
            plannedRoute.add(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)));
            wayPoints.add(startPosition);
        } else {
            // If no location exists, defaults to 0,0 so app doesn't crash
            Marker startPosition = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .title("Current Location Unknown"));
            plannedRoute.add(new LatLng(0,0));
            wayPoints.add(startPosition);
        }
    }

    private void getMapFragmentHandle(){
        // Google (2016) Map Objects [online]
        // Mountain View, California: Google. Available from
        // https://developers.google.com/maps/documentation/android-api/map [Accessed 27 November 2016].
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plan_route, menu);
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

    // Saves the current markers as a route to a database and takes the user to SavedRoutes activity.
    public void saveRouteButton(View view) {
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
            // Initialising mDbHelper. This is needed in the AsyncTask called below.
            mDbHelper = DatabaseHelper.getInstance(this);

            // The below variables need to be initialised before calling the Async. They won't work if put inside the async as they require operations that can't be
            // performed if not on the main thread. This is ok as the main point of Async is calling 'getWritableDatabase' on a different thread.

            // 'tempDistance' used to calculate the distance of the route
            tempDistance = CalculateDistance.getFinalDistance(wayPoints);
            // Temporary string to hold the marker lat/long coordinates - will hold cords for every marker on a new line for easier parsing later.
            // Line below defaults to hold the user's current position (the start point of the route).
            tempLatLong = String.valueOf(latitude) + "," + String.valueOf(longitude) + "\n";
            // now loop through waypoints and add all the cords to tempLatLong
            for (int i = 0; i < wayPoints.size(); i++) {
                Log.e("debugger", "testing");
                tempLatLong = tempLatLong + String.valueOf(wayPoints.get(i).getPosition().latitude) + "," + String.valueOf(wayPoints.get(i).getPosition().longitude + "\n");
            }

            // Calling the async task to write the data to the database
            // An async task is needed here as - following Google developer's recommendations 'getWritableDatabase()' shouldn't be called
            // in the main thread.
            new AsyncTaskSaveRoute().execute();
        }
    }

    public class AsyncTaskGetLocation extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            Log.e("onPreExecute", "huh");
            pd=ProgressDialog.show(PlanRoute.this,"","Please Wait",false);
        }

        @Override
        protected String doInBackground(String... arg0)  {
            try {

                while (mLoc.mGoogleApiClient.isConnecting())
                {
                    // Log.e("doInBackground ", "its connecting");
                    publishProgress();
                    if (mLoc.mGoogleApiClient.isConnected())
                    {
                        Log.e("doInBackground ", "mloc connected!");
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
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

    public class AsyncTaskSaveRoute extends AsyncTask<String, String, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            // Progress dialog to let the user know something is happeneing.
            pd=ProgressDialog.show(PlanRoute.this,"","Please Wait",false);
        }

        @Override
        protected String doInBackground(String... arg0)  {
            try {
                // Gets the data repository in write mode
                db = mDbHelper.getWritableDatabase();

                // Sets the tempRouteName that will be used to write a default route name when the user adds a route.
                // This name will be number of the lastRecord _id + 1.
                tempRouteName = "Route " + String.valueOf( DatabaseHelper.getLast(db, DatabaseContract.SavedRoutesTable.TABLE_NAME)+1);

                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.SavedRoutesTable.COLUMN_NAME_1, tempRouteName);
                values.put(DatabaseContract.SavedRoutesTable.COLUMN_NAME_2, tempLatLong);
                values.put(DatabaseContract.SavedRoutesTable.COLUMN_NAME_3, tempDistance);

                // Insert the data into the SavedRoutes table and return the new _id for this row as a long
                long newRowId = db.insert(DatabaseContract.SavedRoutesTable.TABLE_NAME, null, values);

                // newRowId can now be used as a Foreign key for this table
                ContentValues values2 = new ContentValues();
                values2.put(DatabaseContract.RouteStatisticsTable.COLUMN_NAME_1, String.valueOf(newRowId)); // route_id
                values2.put(DatabaseContract.RouteStatisticsTable.COLUMN_NAME_2, 0); // # times ran
                values2.put(DatabaseContract.RouteStatisticsTable.COLUMN_NAME_3, 0); // best time
                values2.put(DatabaseContract.RouteStatisticsTable.COLUMN_NAME_4, 0); // worst time

                // Insert the new row for the RouteStatisticsTable
                db.insert(DatabaseContract.RouteStatisticsTable.TABLE_NAME, null, values2);

                // Closing db connection
                db.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
            pd.dismiss();
            // Finally start the intent to go to SavedRoutes activity
            Intent intent = new Intent(PlanRoute.this, SavedRoutes.class);
            //start Activity
            startActivity(intent);
        }
    }
}
