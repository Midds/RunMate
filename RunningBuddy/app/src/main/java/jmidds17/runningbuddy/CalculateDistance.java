package jmidds17.runningbuddy;

import android.location.Location;

import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * This class is used in PlanRoute, TrackRun, RunARoute in order to calculate the distance in metres
 * of a set of markers. It will add the distance between each marker before returning the final distance.
 */
public class CalculateDistance {
    // Using Location.distanceBetween to calculate the run distance. Retruns in kilometers.
    // Google (2016) Location [online]
    // Mountain View, California: Google. Available from
    // https://developer.android.com/reference/android/location/Location.html [Accessed 15 December 2016].
    public static double getFinalDistance(List<Marker> routeToMeasure){
        float distance = 0; // double to hold the final tallied distance
        float[] results = new float[routeToMeasure.size()]; // float array to hold the distances between each location

        // looping though each waypoint and adding the distance each time
        for (int i = 0; i < routeToMeasure.size() - 1; i++) {
            Location.distanceBetween(routeToMeasure.get(i).getPosition().latitude, routeToMeasure.get(i).getPosition().longitude,
                    routeToMeasure.get(i+1).getPosition().latitude, routeToMeasure.get(i+1).getPosition().longitude,
                    results);

            // Adding up the distance as it iterates through the way points
            distance = distance + results[0];
        }

        // return distance rounded to 2 decimal places
        return RoundNumber.round(distance, 2);
    }
}
