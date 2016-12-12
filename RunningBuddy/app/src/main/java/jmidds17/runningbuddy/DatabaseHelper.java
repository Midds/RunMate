package jmidds17.runningbuddy;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by James on 11/12/2016.
 * This class is used to actually create the database. Where DatabaseContract defines the schema of
 * the database, this class will be called from my activity to actually create the database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.SavedRoutesTable.SQL_CREATE_ENTRIES);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DatabaseContract.SavedRoutesTable.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    static public int getNumRecords (SQLiteDatabase db, String tableName)
    {
        int numRows = (int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + tableName, null);
        return numRows;
    }
}
