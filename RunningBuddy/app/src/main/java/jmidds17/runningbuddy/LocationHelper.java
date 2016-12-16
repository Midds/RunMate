package jmidds17.runningbuddy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by James on 14/12/2016.
 * This class is used to access current location using Google Play Services Api
 * This is needed as a seperate class, as a number of activities in this app require GPS location to
 * function to their full extent. Therefore having this locationHelper class saves a lot of code from
 * being repeated in the other activities.
 */
public class LocationHelper extends MainActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    //global variables needed for google play services
    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    LocationRequest mLocationRequest = new LocationRequest();
    static public Location mCurrentLocation;
    boolean mRequestingLocationUpdates;
    private String latitude;
    private String longitude;
    private Context mContext;
    boolean upToDate = false;

    public LocationHelper(Context context) {
        Log.e("LocationHelper", "huh");
        mContext = context;

        // Create an instance of GoogleAPIClient.
        buildGoogleApiClient();
        // Create a location request
        createLocationRequest();
        // Performs mGoogleApiClient.connect as well as looping to ensure it finishes connection before continuing.
        connectToApi();
    }

    // called to connect to google play services before other methods are called.
    public void connectToApi(){
        Log.e("connectToApi", "huh");
        // Connect the the GoogleApiClient
        mGoogleApiClient.connect();

        // While loop ensures that mGoogleApiClient.connect(); fully runs and gets the currentLocation before
        // moving on. This is in place to stop getLatitude & getLongitude from returning null or old values.
        // upToDate will evaluate to true upon either successfull connection or unsuccessful or suspended connection,
        // so this should not result in an infinite loop under any circumstances.
        while (!upToDate)
        {
            // wait
        }
    }

    private void buildGoogleApiClient() {
        Log.e("buildGoogleApiClient", "huh");

        // Google (2016) Getting the Last Known Location [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/location/retrieve-current.html [Accessed 23 November 2016].
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    public String getLatitude() {
        Log.e("getLatitude", "huh");



        if (mCurrentLocation != null)
        {
            latitude = String.valueOf(mCurrentLocation.getLatitude());
            Log.e("getLatitude new current", String.valueOf(mCurrentLocation.getLatitude()));

        }
        else if (mLastLocation != null) {
            latitude = String.valueOf(mLastLocation.getLatitude());
            Log.e("getLatitude new last", String.valueOf(mLastLocation.getLatitude()));

        }
        return latitude;
    }

    public String getLongitude() {
        if (mCurrentLocation != null)
        {
            longitude = String.valueOf(mCurrentLocation.getLongitude());
        }
        else if (mLastLocation != null) {
            longitude = String.valueOf(mLastLocation.getLongitude());
        }
        return longitude;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e("onConnected", "huh");

        // getting last know location on the phone - not a new location
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        Log.e("getLatitude con", String.valueOf(mLastLocation.getLatitude()));
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
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        upToDate = true; // Stops the loop in connectToApi() now startLocationUpdates() has been called.
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("onConnectionSuspended", "huh");
        upToDate = true; // Stops the loop in connectToApi() if connection gets suspended

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("onConnectionFailed", "huh");
        upToDate = true; // Stops the loop in connectToApi() if connection fails
    }

    protected void createLocationRequest() {
        Log.e("createLocationRequest", "huh");

        // Google (2016) Changing Location Settings: Set Up a Location Request [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/location/change-location-settings.html [Accessed 23 November 2016].
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationRequestBuilder(){
        Log.e("createLocRequestBuilder", "huh");

        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            createLocationRequest();
            Log.e("LocRequestBuild granted", "huh");

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

    // Google (2016) Receiving Location Updates: Define the Location Update Callback [online]
    // Mountain View, California: Google. Available from
    // https://developer.android.com/training/location/receive-location-updates.html [Accessed 23 November 2016].
    public void onLocationChanged(Location location) {
        Log.e("onLocationChanged", "huh");
        mCurrentLocation = location;
        latitude = String.valueOf(mCurrentLocation.getLatitude());
        longitude = String.valueOf(mCurrentLocation.getLongitude());
    }

    protected void startLocationUpdates() {
        // If user has granted permission for the app to access location
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.e("startLocUpdate granted", "huh");
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
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

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
        Log.e("stopLocationUpdates", "huh");

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

}
