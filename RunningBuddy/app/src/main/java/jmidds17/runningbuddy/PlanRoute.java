package jmidds17.runningbuddy;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
//imports for google play services
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
// imports for adding google map fragment
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

// add for saving data
import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;
import android.net.Uri;


import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO - delete this, remember directions api key
// AIzaSyDvBnRK1dbjoHGo9I_5Hsb0f4bxARcda6U
public class PlanRoute extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {
    //global variables needed for google play services
    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    LocationRequest mLocationRequest = new LocationRequest();
    static public Location mCurrentLocation;
    boolean mRequestingLocationUpdates;
    private GoogleMap customMap;
    private Marker currentLocMarker;
    //String mLastUpdateTime = DateFormat.getDateTimeInstance().format(new Date());
    int markerCount = 1;
    String markerTitles = "Way point " + markerCount;
    PolylineOptions plannedRoute = new PolylineOptions();
    Polyline polyline;

    // Filename for saving route once user clicks save route button
    String filename = "savedroute";


    public void onMapReady(GoogleMap map) {
        customMap = map; // using global variable customMap so it can be changed in other scopes
        // When map is clicked place a marker
        // Rathore, A. (2013) Add Marker on Android Google Map via touch or tap.
        // [stack overflow] 12 September. Available from
        // https://stackoverflow.com/questions/17143129/add-marker-on-android-google-map-via-touch-or-tap [Accessed 27 November 2016].
        customMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                Marker wayPointM = customMap.addMarker(new MarkerOptions()
                        .position(new LatLng(point.latitude, point.longitude))
                        .title(markerTitles)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .draggable(true));

                plannedRoute.add(new LatLng(point.latitude, point.longitude));
                polyline = customMap.addPolyline(plannedRoute);
                wayPoints.add(markerCount - 1, wayPointM);
                markerCount++;
            }
        });
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_route);

        // Getting a handle to the fragment where the map is located
        getMapFragmentHandle();

        // Create an instance of GoogleAPIClient when activity is created(if one doesn't exist).
        buildGoogleApiClient();

        createLocationRequest();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // getting last know location on the phone - not a new location
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // Google (2016) Receiving Location Updates: Request Location Updates [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/location/receive-location-updates.html [Accessed 23 November 2016].

        // below If refers to a boolean flag that is used to track whether user has turned
        // location updates on or off
        // for now i will assume this is true and startLocationUpdates
        //if (mRequestingLocationUpdates) {
        //    startLocationUpdates();
        //}

        // Begin tracking current location
        createLocationRequestBuilder();
        startLocationUpdates();

        // Setup the map
        configureMapDefault();
    }

    private void getMapFragmentHandle(){
        // Google (2016) Map Objects [online]
        // Mountain View, California: Google. Available from
        // https://developers.google.com/maps/documentation/android-api/map [Accessed 27 November 2016].
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void buildGoogleApiClient(){
        // Google (2016) Getting the Last Known Location [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/location/retrieve-current.html [Accessed 23 November 2016].
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    private void createLocationRequestBuilder(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            createLocationRequest();

            // Google (2016) Changing Location Settings: Get Current Location Settings [online]
            // Mountain View, California: Google. Available from
            // https://developer.android.com/training/location/change-location-settings.html [Accessed 23 November 2016].
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            // Check whether current location settings are satisfied
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                            builder.build());
        }
    }
    // Controls what the user first sees on the map (default location, zoom, markers)
    private void configureMapDefault() {
        customMap.clear(); // Clears current marker before adding an updated one
        if (mCurrentLocation != null) {
            // Google (2016) CameraUpdateFactory [online]
            // Mountain View, California: Google. Available from
            // https://developers.google.com/android/reference/com/google/android/gms/maps/CameraUpdateFactory [Accessed 27 November 2016].
            customMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 15) );
            currentLocMarker = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                    .title("You are here"));
            plannedRoute.add(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));
        }
        else if (mLastLocation != null) {
            customMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15) );
            currentLocMarker = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .title("You are here"));
            plannedRoute.add(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
        } else {
            // If no location exists, defaults to 0,0 so app doesn't crash
            currentLocMarker = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .title("Current Location Unknown"));
        }
    }

    private void addMarker(){

    }

    protected void createLocationRequest() {
        // Google (2016) Changing Location Settings: Set Up a Location Request [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/location/change-location-settings.html [Accessed 23 November 2016].
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    List<Marker> wayPoints = new ArrayList<Marker>();

    public void textChangeButton(View view) {
        Marker wayPointM = customMap.addMarker(new MarkerOptions()
                .position(currentLocMarker.getPosition())
                .title(markerTitles)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .draggable(true));
        wayPoints.add(wayPointM);
        markerCount++;
    }
    /*
    public void saveRoute(View view) {
        // Creating a file in local storage to save routes to
        FileOutputStream outputStream;
        File file = getFileStreamPath(filename);

        // Check if there are any waypoints to be saved so it doesn't try to write a null value to file
        if (wayPoints.size() > 0) {
            // Check if file exists. If not - make it.
            if (file == null || !file.exists()) {
                try {
                    outputStream = openFileOutput(filename, MODE_PRIVATE);
                    // loops through waypoints list and saves every marker location into local storage
                    for (int i = 0; i < wayPoints.size(); i++) {
                        outputStream.write(String.valueOf(wayPoints.get(i).getPosition().latitude + "," + wayPoints.get(i).getPosition().longitude).getBytes());
                        outputStream.write("\r\n".getBytes());
                    }
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // if file already exists then it will append to that file instead of making a new one
            else if (file.exists())
            {
                try {
                    outputStream = openFileOutput(filename, Context.MODE_APPEND);
                    for (int i = 0; i < wayPoints.size(); i++) {
                        outputStream.write(String.valueOf(wayPoints.get(i).getPosition().latitude + "," + wayPoints.get(i).getPosition().longitude).getBytes());
                        outputStream.write("\r\n".getBytes());
                    }
                    outputStream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // show toast message for successful save
            Context context = getApplicationContext();
            CharSequence text = "Route saved to local storage!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        // If there are no waypoints it will tell user that nothing was saved
        else if (wayPoints.size() == 0)
        {
            // show toast message for successful save
            Context context = getApplicationContext();
            CharSequence text = "Save failed. No Waypoints to save!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
    */

    public void saveRoute(View view) {
        String tempLatLong = "";
        String tempRouteName;

        DatabaseHelper mDbHelper = new DatabaseHelper(getBaseContext());
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Sets the tempRouteName that will be used to write a default route name when the user adds a route.
        // This name will be number of records + 1. User can configure their own name later if they want.
        tempRouteName = String.valueOf(DatabaseHelper.getNumRecords(db, DatabaseContract.SavedRoutesTable.TABLE_NAME) + 1);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SavedRoutesTable.COLUMN_NAME_1, tempRouteName);
        for (int i = 0; i < wayPoints.size(); i++) {
            tempLatLong = tempLatLong + String.valueOf(wayPoints.get(i).getPosition().latitude) + "," + String.valueOf(wayPoints.get(i).getPosition().longitude + "\n");
        }
        values.put(DatabaseContract.SavedRoutesTable.COLUMN_NAME_2, tempLatLong);
        values.put(DatabaseContract.SavedRoutesTable.COLUMN_NAME_3, "Test Distance");


        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(DatabaseContract.SavedRoutesTable.TABLE_NAME, null, values);

        mDbHelper.close();
        db.close();


    }

    public void getMarkerPos(){
    }

    public void removeLastMarker(View view){
        markerCount = 1; // resets marker count
        wayPoints.clear(); // clears the listarray of waypoints
        customMap.clear(); // removes all custom markers from map
        plannedRoute = new PolylineOptions(); // Clears the polyline route on reset
        updateUI(); // replaces currentlocation origin marker on map
    }


    // Google (2016) Receiving Location Updates: Define the Location Update Callback [online]
    // Mountain View, California: Google. Available from
    // https://developer.android.com/training/location/receive-location-updates.html [Accessed 23 November 2016].
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        boolean upDateFlag = false;
        //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        // If the user is currently planning a route the map will not update upon the gps location changing
        // If there are no other markers then the map will stay updated with the users movement
        if (wayPoints.size() <= 0) {
            updateUI(); // uncomment to keep map updated on location change (prevents planning a route when moving as it resets the map)
        }
    }

    private void updateUI() {
        configureMapDefault();
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

    protected void startLocationUpdates() {
        // If user has granted permission for the app to access location
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.

            // Google (2016) Receiving Location Updates: Request Location Updates [online]
            // Mountain View, California: Google. Available from
            // https://developer.android.com/training/location/receive-location-updates.html [Accessed 23 November 2016].
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }


    // onPause and stopLocationUpdates can be used to stop updating the location when the activity is
    // no longer in focus. On resume will restart the location updates when the activity is back in focus.

    // Google (2016) Receiving Location Updates: Stop Location Updates [online]
    // Mountain View, California: Google. Available from
    // https://developer.android.com/training/location/receive-location-updates.html [Accessed 24 November 2016].

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
        startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
