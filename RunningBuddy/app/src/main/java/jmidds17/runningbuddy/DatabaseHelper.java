package jmidds17.runningbuddy;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Where DatabaseContract defines the schema of the database, this class will be called from an activity to actually
 * create the database. As with the DatabaseContract. Google's developer documentation was used heavily to create this class.
 * Google (2016) Saving Data in SQL Databases: Define a Schema and Contract [online]
 * Mountain View, California: Google. Available from
 * https://developer.android.com/training/basics/data-storage/databases.html [Accessed 11 December 2016].
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Using a static getInstance method ensures that 'only one DatabaseHelper class will ever exist
    // at any given time' (Lockwood, 2012). This was needed as i was getting some database leak warnings in logcat.
    // The following variable mInstance and method getInstance are taken from
    // Lockwood, A. (2012) Correctly Managing your SQLite Database. [blog entry] 21 May. Available from
    // http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html [Accessed 18 December 2016].
    private static DatabaseHelper mInstance = null;
    public static DatabaseHelper getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }

    // Set to private to prevent direct instantiation, can instead make call to getInstance(above) instead
    private DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.SavedRoutesTable.SQL_CREATE_ENTRIES);
        db.execSQL(DatabaseContract.RouteStatisticsTable.SQL_CREATE_ENTRIES);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DatabaseContract.SavedRoutesTable.SQL_DELETE_ENTRIES);
        db.execSQL(DatabaseContract.RouteStatisticsTable.SQL_CREATE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // returns number of records
    static public int getNumRecords (SQLiteDatabase db, String tableName)
    {
        int numRows = (int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + tableName, null);
        return numRows;
    }

    // returns the unique id of the last record in the database
    static public int getLast(SQLiteDatabase db, String tableName)
    {
        int numRows = (int) DatabaseUtils.longForQuery(db, "SELECT max(_id) FROM " + tableName, null);
        return numRows;
    }
}
