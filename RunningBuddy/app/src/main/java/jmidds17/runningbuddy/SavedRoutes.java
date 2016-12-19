package jmidds17.runningbuddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;

import javax.security.auth.login.LoginException;


public class SavedRoutes extends Activity {
    DatabaseHelper mDbHelper = DatabaseHelper.getInstance(this);

    Cursor c;
    SQLiteDatabase db;


    ArrayList<Route> arrayOfRoutes;
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

        // Construct the data source
        arrayOfRoutes = new ArrayList<Route>();
        // Create the adapter to convert the array to views
        adapter = new RoutesAdapter(this, arrayOfRoutes);
        // Attach the adapter to a ListView
        listView = (ListView) findViewById(R.id.routesListView);

        // Call async class which will then open the database
        new AsyncTaskGetSavedData().execute();
    }

    public void planNewRoute(View view) {
        //create an intent to start PlanRoute activity
        Intent intent = new Intent(this, PlanRoute2.class);
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

        boolean isConnected;
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            Log.e("onPreExecute", "huh");
            pd=ProgressDialog.show(SavedRoutes.this,"","Please Wait",false);
        }

        @Override
        protected String doInBackground(String... arg0)  {
            try {
                Log.e("doInBackground", "huh");
                ListView routesList = (ListView)findViewById(R.id.routesListView);
                bindRoutesToList(routesList);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(String... progress){

        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
            Log.e("onPostExecute", "huh");

            // If no routes exist then display some text
            checkRoutesExist();

            pd.dismiss();
        }
    }


    public void checkRoutesExist(){
        ListView routesList = (ListView)findViewById(R.id.routesListView);
        if (routesList.getCount() < 1) {
            findViewById(R.id.noRoutesText).setVisibility(View.VISIBLE);
        }
    }
    public void bindRoutesToList(ListView routesList) {
        db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.SavedRoutesTable._ID,
                DatabaseContract.SavedRoutesTable.COLUMN_NAME_1,
                DatabaseContract.SavedRoutesTable.COLUMN_NAME_2,
                DatabaseContract.SavedRoutesTable.COLUMN_NAME_3
        };

        // Filter results WHERE "title" = 'Route 1'
        //String selection = DatabaseContract.SavedRoutesTable.COLUMN_NAME_1 + " = ?";
        String selection = "SELECT * FROM " + DatabaseContract.SavedRoutesTable.TABLE_NAME;
        Log.e("name of column", DatabaseContract.SavedRoutesTable.COLUMN_NAME_1);
        String[] selectionArgs = { "Route 1" };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.SavedRoutesTable.COLUMN_NAME_2 + " DESC";

        /* c = db.query(
                DatabaseContract.SavedRoutesTable.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        ); */

        c = db.rawQuery(selection, null);

        //String itemName = c.getString(
        //        c.getColumnIndexOrThrow(DatabaseContract.SavedRoutesTable.COLUMN_NAME_1)
        // );


        //ArrayAdapter<Route> arrayOfRoutes =
        //        new ArrayAdapter<Route>(this, android.R.layout.andro, items);


        if(c != null && c.moveToFirst()) {
            do {
                // Add item to adapter
                Route newRoute = new Route(
                        c.getInt(c.getColumnIndex("_id")),
                        c.getString(c.getColumnIndex("RunName")) + "\n",
                        c.getString(c.getColumnIndex("RunDistance")),
                        c.getString(c.getColumnIndex("RunWaypoints")));
                adapter.add(newRoute);

            } while (c.moveToNext());
        }

        listView.setAdapter(adapter);
        ListView lv1 = (ListView)findViewById(R.id.routesListView);
    }

    public void removeRoute(Route route){
        // Removing the selected route from the list view
        adapter.remove(route);

        // Deleting the selected route from the database, adapted from
        // Google (2016) Saving Data in SQL Databases: Delete Information from a Database [online]
        // Mountain View, California: Google. Available from
        // https://developer.android.com/training/basics/data-storage/databases.html#DeleteDbRow [Accessed 17 December 2016].

        // Define 'where' part of query.
        String selection = DatabaseContract.SavedRoutesTable._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(route.id) };
        // Issue SQL statement.
        db.delete(DatabaseContract.SavedRoutesTable.TABLE_NAME, selection, selectionArgs);

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
            TextView tv1 = (TextView) convertView.findViewById(R.id.tvName);
            TextView tv2 = (TextView) convertView.findViewById(R.id.tvLength);
            // Populate the data into the template view using the data object
            tv1.setText(route.name);
            tv2.setText(route.length);


            // Find button
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

            // Lookup view for data population
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




