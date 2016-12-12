package jmidds17.runningbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    /** Called when user touches the CLICK ME! button */
    public void startRoutePlan(View view) {

        // create an intent to start the activity called MainActivity
        Intent intent = new Intent(this, PlanRoute.class);
        // start Activity
        startActivity(intent);
    }

    public void showRoutes(View view) {

        // create an intent to start the activity called MainActivity
        Intent intent = new Intent(this, SavedRoutes.class);
        // start Activity
        startActivity(intent);
    }

    public void configureTrack(View view) {
        // create an intent to start the activity called MainActivity
        Intent intent = new Intent(this, TrackRun.class);
        // start Activity
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
