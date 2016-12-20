package jmidds17.runningbuddy;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by James on 15/12/2016.
 */
// Class that implements a Route as an object to hold the data pulled from the database
// Implements parcelable - this lets a Route object get passed between activities (needed for the run button)
// In order to implement Parcelable i have adapted code from the google developer documentation here.
// Google (2016) Parcelable [online]
// Mountain View, California: Google. Available from
// https://developer.android.com/reference/android/os/Parcelable.html [Accessed 18 December 2016].
public class Route implements Parcelable {
    public Integer id;
    public String name;
    public String length;
    public String waypoints;
    public int numberTimesRan;
    public String bestTime;
    public String worstTime;


    public Route(Integer id, String name, String length, String waypoints, int numbertimesran, String besttime, String worsttime) {
        this.id = id;
        this.name = name;
        this.length = length;
        this.waypoints = waypoints;
        this.numberTimesRan = numbertimesran;
        this.bestTime = besttime;
        this.worstTime = worsttime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(name);
        out.writeString(length);
        out.writeString(waypoints);
        out.writeInt(numberTimesRan);
        out.writeString(bestTime);
        out.writeString(worstTime);
    }

    public static final Parcelable.Creator<Route> CREATOR
            = new Parcelable.Creator<Route>() {
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    private Route(Parcel in) {
        id = in.readInt();
        name = in.readString();
        length = in.readString();
        waypoints = in.readString();
        numberTimesRan = in.readInt();
        bestTime = in.readString();
        worstTime = in.readString();
    }
}