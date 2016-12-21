package jmidds17.runningbuddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;


public class SavedRoutes extends Activity {

    // global variables for sqlite
    SQLiteDatabase db;
    RoutesAdapter adapter;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_routes);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Construct the data source that will populate the listview
        ArrayList<Route> arrayOfRoutes = new ArrayList<Route>();
        // Create the adapter to convert the array to views
        adapter = new RoutesAdapter(this, arrayOfRoutes);
        // Attach the adapter to a ListView
        listView = (ListView) findViewById(R.id.routesListView);

        // Call async class which will then open the database connection
        new AsyncTaskGetSavedData().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close(); // close the database connection when activity goes out of view
    }

    // new route button
    public void planNewRoute(View view) {
        //create an intent to start PlanRoute activity
        Intent intent = new Intent(this, PlanRoute.class);
        //start Activity
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_saved_routes, menu);
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

    public class AsyncTaskGetSavedData extends AsyncTask<String, String, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd=ProgressDialog.show(SavedRoutes.this,"","Please Wait",false);
        }

        @Override
        protected String doInBackground(String... arg0)  {
            try {
                // binding data to the listview
                ListView routesList = (ListView)findViewById(R.id.routesListView);
                bindRoutesToList(routesList);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
            // If no routes exist then display some text
            checkRoutesExist();
            pd.dismiss();
        }
    }


    public void checkRoutesExist(){
        ListView routesList = (ListView)findViewById(R.id.routesListView);
        if (routesList.getCount() < 1) {
            findViewById(R.id.noRoutesText).setVisibility(View.VISIBLE);
            findViewById(R.id.viewBox).setVisibility(View.INVISIBLE);
        }
    }

    // called in async task
    public void bindRoutesToList(ListView routesList) {
        DatabaseHelper mDbHelper = DatabaseHelper.getInstance(this);
        db = mDbHelper.getReadableDatabase(); //important that this is called in an async task as it can take a long time

        // Crafting the raw sql query that will join the two tables with any relevant columns for this activity
        String rawJoinQuery = "SELECT "
                + DatabaseContract.SavedRoutesTable.TABLE_NAME + "."
                + DatabaseContract.SavedRoutesTable._ID + ", " // route id
                + DatabaseContract.SavedRoutesTable.TABLE_NAME + "."
                + DatabaseContract.SavedRoutesTable.COLUMN_NAME_1 + ", " // route name
                + DatabaseContract.SavedRoutesTable.TABLE_NAME + "."
                + DatabaseContract.SavedRoutesTable.COLUMN_NAME_3 + ", " // route distance
                + DatabaseContract.SavedRoutesTable.TABLE_NAME + "."
                + DatabaseContract.SavedRoutesTable.COLUMN_NAME_2 + ", " // route waypoints
                + DatabaseContract.RouteStatisticsTable.TABLE_NAME + "."
                + DatabaseContract.RouteStatisticsTable.COLUMN_NAME_2 + ", " // # times ran
                + DatabaseContract.RouteStatisticsTable.TABLE_NAME + "."
                + DatabaseContract.RouteStatisticsTable.COLUMN_NAME_3 + ", " // best time
                + DatabaseContract.RouteStatisticsTable.TABLE_NAME + "."
                + DatabaseContract.RouteStatisticsTable.COLUMN_NAME_4 // worst time
                + " FROM '"
                + DatabaseContract.RouteStatisticsTable.TABLE_NAME
                + "' INNER JOIN '"
                + DatabaseContract.SavedRoutesTable.TABLE_NAME
                + "' ON "
                + DatabaseContract.RouteStatisticsTable.TABLE_NAME + "." // where route_id = _id
                + DatabaseContract.RouteStatisticsTable.COLUMN_NAME_1
                + "="
                + DatabaseContract.SavedRoutesTable.TABLE_NAME + "."
                + DatabaseContract.SavedRoutesTable._ID;

        // querying the database
        Cursor c = db.rawQuery(rawJoinQuery, null);

        if(c != null && c.moveToFirst()) {
            do {
                // Add item to adapter
                Route newRoute = new Route(
                        c.getInt(c.getColumnIndex("_id")),
                        c.getString(c.getColumnIndex("RunName")),
                        c.getDouble(c.getColumnIndex("RunDistance")),
                        c.getString(c.getColumnIndex("RunWaypoints")),
                        c.getInt(c.getColumnIndex("TimesRan")),
                        c.getDouble(c.getColumnIndex("BestTime")),
                        c.getDouble(c.getColumnIndex("WorstTime")));
                adapter.add(newRoute);

            } while (c.moveToNext());
        }

        c.close(); //closing cursor

        listView.setAdapter(adapter);
    }

    public void removeRoute(Route route){
        // Removing the selected route from the list view
        adapter.remove(route);

        // Deleting the selected route from the database, adapted from
        // Google (2016) Saving Data in SQL Databases: Delete Information from a Database [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/basics/data-storage/databases.html#DeleteDbRow [Accessed 17 December 2016].

        // remove the selected route from the SavedRuns table
        // Define 'where' part of query.
        String selection = DatabaseContract.SavedRoutesTable._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(route.id) };
        // Issue SQL statement.
        db.delete(DatabaseContract.SavedRoutesTable.TABLE_NAME, selection, selectionArgs);

        // remove the selected route from the RoutesStatistics table
        // Define 'where' part of query.
        String selection2 = DatabaseContract.RouteStatisticsTable._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs2 = { String.valueOf(route.id) };
        // Issue SQL statement.
        db.delete(DatabaseContract.RouteStatisticsTable.TABLE_NAME, selection2, selectionArgs2);

        // If no routes exist after deleting then display some text
        checkRoutesExist();
    }

    public void loadRoute(Route route){
        //create an intent to start RunARoute activity
        Intent intent = new Intent(this, RunARoute.class);
        // pass the route that the use just clicked on to the new activity
        intent.putExtra("route", route);
        //start Activity
        startActivity(intent);
    }

    public void viewRoute(Route route){
        String distanceType = "m"; // used to assign either metres or kilometers depending on the value of the distance

        // setting  route name
        TextView tv1 = (TextView)findViewById(R.id.viewBoxName);
        tv1.setText(route.name);
        tv1.setVisibility(View.VISIBLE);

        // if route length is more than 1k meters then convert to kilometers
        if (route.length > 999){
            route.length = route.length/1000;
            distanceType = "km";
        }

        // setting route distance
        TextView tv2 = (TextView)findViewById(R.id.viewBoxDistance);
        tv2.setText("Distance: " + RoundNumber.round(route.length, 2) + distanceType);
        tv2.setVisibility(View.VISIBLE);

        // setting route best & worst times
        TextView tv3 = (TextView)findViewById(R.id.viewBoxStats);
        tv3.setText("Best Time: " + RoundNumber.round(route.bestTime, 1) + "s" + "   Worst Time: " + RoundNumber.round(route.worstTime, 1) + "s");
        tv3.setVisibility(View.VISIBLE);

        // setting route counter
        TextView tv4 = (TextView)findViewById(R.id.viewBoxNumberRan);
        tv4.setText("Run Counter: " + route.numberTimesRan);
        tv4.setVisibility(View.VISIBLE);

        // removing the image that was in the viewBox
        ImageView iv2 = (ImageView)findViewById(R.id.runnerIcon);
        iv2.setVisibility(View.INVISIBLE);

        // removing the text that was in the viewBox
        TextView tv5 = (TextView)findViewById(R.id.viewBoxSeeMore);
        tv5.setVisibility(View.INVISIBLE);
    }

    // RoutesAdapter is used to bind routes to the listview, this was adapted from a tutorial by CodePath.
    // Need a custom adapter rather than the provided simple default ones so i can add buttons to each item.
    // CodePath (2016) Using an ArrayAdapter with ListView [online]
    // San Francisco, California: CodePath. Available from
    // https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView [Accessed 17 December 2016].
    public class RoutesAdapter extends ArrayAdapter<Route> {
        public RoutesAdapter(Context context, ArrayList<Route> routes) {
            super(context, 0, routes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Route route = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_route, parent, false);
            }
            // Lookup view for data population
            TextView tv1 = (TextView) convertView.findViewById(R.id.routeName);
            tv1.setText(route.name);

            // Delete button
            Button deleteButton = (Button) convertView.findViewById(R.id.deleteButton);
            // Cache row position for the button using `setTag`
            deleteButton.setTag(position);
            // Attach the click event handler
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    // Access the row position here to get the correct data item
                    Route route = getItem(position);
                    // Call removeRoute method
                    removeRoute(route);
                }
            });

            // Run button
            Button runButton = (Button) convertView.findViewById(R.id.runButton);
            // Cache row position inside the button using `setTag`
            runButton.setTag(position);
            // Attach the click event handler
            runButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    // Access the row position here to get the correct data item
                    Route route = getItem(position);
                    // call loadRoute method, this will take the selected route and send it's data
                    // to another activity.
                    loadRoute(route);
                }
            });

            // View button
            Button viewButton = (Button) convertView.findViewById(R.id.viewButton);
            // Cache row position for the button using `setTag`
            viewButton.setTag(position);
            // Attach the click event handler
            viewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    // Access the row position and get the selected route
                    Route route = getItem(position);
                    // Call removeRoute method and give it the selected route as a parameter
                    viewRoute(route);
                }
            });
            // Return the completed view to render on screen
            return convertView;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("onStop", "closing db");
        // Closing the database if the activity goes out of view
        db.close();
    }
}




