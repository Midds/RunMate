package jmidds17.runningbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

/**
 * Created by James on 14/12/2016.
 * This class is used to access current location using Google Play Services Api
 */

public class LocationHelper extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    //global variables needed for google play services
    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    LocationRequest mLocationRequest = new LocationRequest();
    static public Location mCurrentLocation;
    boolean mRequestingLocationUpdates;
    private String latitude;
    private String longitude;
    private Context mContext;

    public LocationHelper(Context context) {
        mContext = context;
        // Create an instance of GoogleAPIClient.
        buildGoogleApiClient();
        // Create a location request and connect to api
        createLocationRequest();
        connectToApi();
    }

    public void connectToApi (){
        // Performs mGoogleApiClient.connect
        mGoogleApiClient.connect();
    }

    public void buildGoogleApiClient() {
        // Google (2016) Getting the Last Known Location [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/location/retrieve-current.html [Accessed 23 November 2016].
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    public String getLatitude() {
        // Tried to update current location if it isn't null
        if (mCurrentLocation != null) {
            latitude = String.valueOf(mCurrentLocation.getLatitude());
        }
        // If current location is null then tries to use mLastLocation instead (Fused Service Provider)
        // This might be out of date but it is better than nothing.
        else if (mLastLocation != null) {
            latitude = String.valueOf(mLastLocation.getLatitude());
        }
        return latitude;
    }

    public String getLongitude() {
        if (mCurrentLocation != null) {
            longitude = String.valueOf(mCurrentLocation.getLongitude());
        }
        else if (mLastLocation != null) {
            longitude = String.valueOf(mLastLocation.getLongitude());
        }
        return longitude;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // getting last know location on the phone - not a new location
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if (mLastLocation != null) {
            latitude = (String.valueOf(mLastLocation.getLatitude()));
            longitude = (String.valueOf(mLastLocation.getLongitude()));
        }

        // Google (2016) Receiving Location Updates: Request Location Updates [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/location/receive-location-updates.html [Accessed 23 November 2016].
        // Begin tracking current location
        createLocationRequestBuilder();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("LocationHelper", "Connection to mGoogleApiClient suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("LocationHelper", "Connection to mGoogleApiClient failed");
    }

    protected void createLocationRequest() {
        // Google (2016) Changing Location Settings: Set Up a Location Request [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/location/change-location-settings.html [Accessed 23 November 2016].
        // Location request is set to get location every 10 seconds or 5 at fastest. This could be made longer to preserve bettery life.
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationRequestBuilder(){

        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

    // Google (2016) Receiving Location Updates: Define the Location Update Callback [online]
    // Mountain View, California: Google. Available from
    // https://developer.android.com/training/location/receive-location-updates.html [Accessed 23 November 2016].
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        latitude = String.valueOf(mCurrentLocation.getLatitude());
        longitude = String.valueOf(mCurrentLocation.getLongitude());

        // When location changes, finds out what activity LocationHelper was called from by checking
        // mContext. Can then call different methods in the parent class using the switch statement.
        String parentActivity = mContext.getClass().getSimpleName();
        switch (parentActivity){
            case "MainActivity":
                break;
            case "PlanRoute":
                break;
            case "TrackRun": TrackRun.getUpdates();
                break;
            case "RunARoute": RunARoute.getUpdates();
                break;
            default:
                break;
        }
    }

    protected void startLocationUpdates() {
        // If user has granted permission for the app to access location
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Google (2016) Receiving Location Updates: Request Location Updates [online]
            // Mountain View, California: Google. Available from
            // https://developer.android.com/training/location/receive-location-updates.html [Accessed 23 November 2016].
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
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

    // stopLocationUpdates will disconnect from the google api client
    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        else if (!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }
}
