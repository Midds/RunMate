package jmidds17.runningbuddy;

import android.provider.BaseColumns;

/**
 * Created by James on 11/12/2016.
 * This is a class that defines the schema of the sqlite database that will save runs and stats about
 * these runs. It will define tables and columns.
 *
 *  Google (2016) Saving Data in SQL Databases: Define a Schema and Contract [online]
 *  Mountain View, California: Google. Available from
 *  https://developer.android.com/training/basics/data-storage/databases.html [Accessed 11 December 2016].
 */
public class DatabaseContract {

    public static final  int    DATABASE_VERSION   = 1;
    public static final  String DATABASE_NAME      = "database.db";
    private static final String TEXT_TYPE          = " TEXT";
    private static final String COMMA_SEP          = ",";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DatabaseContract() {}

    /* Inner class that defines the table contents */
    public static class SavedRoutesTable implements BaseColumns {
        public static final String TABLE_NAME = "SavedRuns";
        public static final String COLUMN_NAME_1 = "RunName";
        public static final String COLUMN_NAME_2 = "RunWaypoints";
        public static final String COLUMN_NAME_3 = "RunDistance";

        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SavedRoutesTable.TABLE_NAME + " (" +
                        SavedRoutesTable._ID + " INTEGER PRIMARY KEY," +
                        SavedRoutesTable.COLUMN_NAME_1 + TEXT_TYPE + COMMA_SEP +
                        SavedRoutesTable.COLUMN_NAME_2 + TEXT_TYPE + COMMA_SEP +
                        SavedRoutesTable.COLUMN_NAME_3 + TEXT_TYPE + " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + SavedRoutesTable.TABLE_NAME;
    }

    public static class RouteStatisticsTable implements BaseColumns {
        public static final String TABLE_NAME = "Route Statistics";
        public static final String COLUMN_NAME_1 = "# Times Ran";
        public static final String COLUMN_NAME_2 = "Best Time";
        public static final String COLUMN_NAME_3 = "Worst Time";
        public static final String COLUMN_NAME_4 = "Average Time";

        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SavedRoutesTable.TABLE_NAME + " (" +
                        RouteStatisticsTable._ID + " INTEGER PRIMARY KEY," +
                        RouteStatisticsTable.COLUMN_NAME_1 + TEXT_TYPE + COMMA_SEP +
                        RouteStatisticsTable.COLUMN_NAME_2 + TEXT_TYPE + COMMA_SEP +
                        RouteStatisticsTable.COLUMN_NAME_3 + TEXT_TYPE + COMMA_SEP +
                        RouteStatisticsTable.COLUMN_NAME_4 + TEXT_TYPE + " )";
    }

}