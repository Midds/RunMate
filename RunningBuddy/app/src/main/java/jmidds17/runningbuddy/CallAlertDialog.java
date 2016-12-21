package jmidds17.runningbuddy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

/**
 * Created by James on 20/12/2016.
 * This class is used to open an alert dialog that prompts the user to activate location settings
 * and is used in many different classes in exception handling.
 * This class is adapted from code from
 * Google (2016) Dialogs: Creating a Dialog Fragment [online]
 * Mountain View, California: Google. Available from
 * https://developer.android.com/guide/topics/ui/dialogs.html [Accessed 20 December 2016].
 */
public class CallAlertDialog {
    static Context mContext;

    // NOTE: If user turns location on for the first time - the location will be null until the phones location changes (sending location in emulator)
    public static void alert(final Context context) {
        mContext = context;
        String yes = "Go to Settings";
        String no = "Return to App";

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Location services may not be activated");
        builder.setMessage("Location services are required to be turned on and set to high accuracy to use this app. \n\nWould you like to turn them on?");
        builder.setPositiveButton(yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Opens android location settings if user clicks yes
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }

        });
        builder.setNegativeButton(no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // If user clicks no, does nothing - returns to app
            }
        });
        builder.show();
    }
}
