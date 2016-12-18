package jmidds17.runningbuddy;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

import java.util.ArrayList;
import java.util.List;

public class PlanRoute2 extends Activity implements OnMapReadyCallback {
    //Global Variables
    LocationHelper mLoc;

    private GoogleMap customMap;
    private Marker currentLocMarker;
    public String longitude = "-0.5431253";
    static String latitude = "53.2260276";
    //String mLastUpdateTime = DateFormat.getDateTimeInstance().format(new Date());
    int markerCount = 1;
    String markerTitles = "Way point " + markerCount;
    PolylineOptions plannedRoute = new PolylineOptions();
    PolylineOptions tempRoute = new PolylineOptions();
    Polyline polyline;
    List<Marker> wayPoints = new ArrayList<Marker>();

    // Filename for saving route once user clicks save route button
    String filename = "savedroute";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("onCreate", "huh");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_route);


        // Getting a handle to the fragment where the map is located
        getMapFragmentHandle();

        // creating instance of locationhelper.
        mLoc = new LocationHelper(PlanRoute2.this);

    }

    public void textChangeButton(View view) {
        mLoc = new LocationHelper(PlanRoute2.this);

        latitude = mLoc.getLatitude();
        longitude = mLoc.getLongitude();

        configureMapDefault();
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
        // Temporary string to hold the marker lat/long coordinates - will hold cords for every marker on a new line for easier parsing later.
        // Defaulted to hold the user's current position.
        String tempLatLong = String.valueOf(latitude) + "," + String.valueOf(longitude) + "\n";
        // String to hold the default name of each route, will be configurable by user later
        String tempRouteName;

        DatabaseHelper mDbHelper = new DatabaseHelper(getBaseContext());
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

        mDbHelper.close();
        db.close();
    }


    // Gets called when app comes back into view eg after user has hit the home screen and returns to app screen.
    @Override
    public void onResume() {
        Log.e("TAG", "onResume ");
        super.onResume();

        // Getting new location coordinates (before configuring map with these coordinates)
        latitude = mLoc.getLatitude();
        longitude = mLoc.getLongitude();
    }

    // Resets the map
    public void removeLastMarker(View view){
        markerCount = 1; // resets marker count
        wayPoints.clear(); // clears the listarray of waypoints
        customMap.clear(); // removes all custom markers from map
        plannedRoute = new PolylineOptions(); // Clears the polyline route on reset
        updateUI(); // replaces currentlocation origin marker on map
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

            // Will run when the map is tappen on - this is used to add markers to plot a route
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

        configureMapDefault();
    }

    // Controls what the user first sees on the map (default location, zoom, markers)
    private void configureMapDefault() {
        customMap.clear(); // Clears current marker before adding an updated one
        if (latitude != null) {
            // Google (2016) CameraUpdateFactory [online]
            // Mountain View, California: Google. Available from
            // https://developers.google.com/android/reference/com/google/android/gms/maps/CameraUpdateFactory [Accessed 27 November 2016].
            customMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 15) );
            currentLocMarker = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                    .title("You are here"));
            plannedRoute.add(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)));
        }
        else if (latitude != null) {
            customMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)), 15));
            currentLocMarker = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)))
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
                .findFragmentById(R.id.map);
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


}
