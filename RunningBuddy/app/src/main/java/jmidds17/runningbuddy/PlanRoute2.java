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
    public Double longitude = -0.5431253;
    static Double latitude = 53.2260276;
    //String mLastUpdateTime = DateFormat.getDateTimeInstance().format(new Date());
    int markerCount = 1;
    String markerTitles = "Way point " + markerCount;
    PolylineOptions plannedRoute = new PolylineOptions();
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
        Marker wayPointM = customMap.addMarker(new MarkerOptions()
                .position(currentLocMarker.getPosition())
                .title(markerTitles)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .draggable(true));
        wayPoints.add(wayPointM);
        markerCount++;
    }

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


    // Gets called when app comes back into view eg after user has hit the home screen and returns to app screen.
    @Override
    public void onResume() {
        Log.e("TAG", "onResume ");
        super.onResume();
    }

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

        configureMapDefault();
    }

    // Controls what the user first sees on the map (default location, zoom, markers)
    private void configureMapDefault() {
        customMap.clear(); // Clears current marker before adding an updated one
        if (latitude != null) {
            // Google (2016) CameraUpdateFactory [online]
            // Mountain View, California: Google. Available from
            // https://developers.google.com/android/reference/com/google/android/gms/maps/CameraUpdateFactory [Accessed 27 November 2016].
            customMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.62227, 1.2608687), 15) );
            currentLocMarker = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(51.62227, 1.2608687))
                    .title("You are here"));
            plannedRoute.add(new LatLng(51.62227,1.2608687));
        }
        else if (latitude != null) {
            customMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
            currentLocMarker = customMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title("You are here"));
            plannedRoute.add(new LatLng(latitude,longitude));
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
