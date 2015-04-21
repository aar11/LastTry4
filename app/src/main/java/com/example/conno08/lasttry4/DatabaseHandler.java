package com.example.conno08.lasttry4;

/**
 * Created by conno08 on 20/04/2015.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    private SQLiteDatabase db;
    //Database Version
    private static final int DATABASE_VERSION = 1;
    //Database Name
    private static final String DATABASE_NAME = "trafficManager";
    //Table Name
    private static final String TABLE_THETRAFFIC = "thetraffics";
    //Table Columns
    private static final String KEY_ID = "id";
    private static final String KEY_LOCATION = "location";
    //private static final String KEY_PH_NO = "phone_number";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Create Table Method
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_THETRAFFIC_TABLE = "CREATE TABLE " + TABLE_THETRAFFIC + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LOCATION + " TEXT" + ")";
        db.execSQL(CREATE_THETRAFFIC_TABLE);
    }

    // If table already exists it is dropped and created again
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_THETRAFFIC);

        // Create tables again
        onCreate(db);
    }

    //Add a new location to the traffic table
    void addTraffic(TrafficInfo traffic) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LOCATION, traffic.getLocation()); // Contact Name
        //values.put(KEY_PH_NO, contact.getPhoneNumber()); // Contact Phone

        // Inserting Row
        db.insert(TABLE_THETRAFFIC, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    //Gets a particular location
    TrafficInfo getTraffic(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_THETRAFFIC, new String[] { KEY_ID,
                        KEY_LOCATION}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        TrafficInfo contact = new TrafficInfo(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1));
        // return contact
        return contact;
    }

    //Gets all the locations
    public List<TrafficInfo> getAllTraffic() {
        List<TrafficInfo> trafficList = new ArrayList<TrafficInfo>();
        // Select All Query
        String selectQuery = "SELECT " + KEY_LOCATION  + " FROM " + TABLE_THETRAFFIC;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TrafficInfo traffic = new TrafficInfo();
                //traffic.setID(Integer.parseInt(cursor.getString(0)));
                traffic.setLocation(cursor.getString(0));
                //contact.setPhoneNumber(cursor.getString(2));
                // Adding contact to list
                trafficList.add(traffic);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return location list
        return trafficList;
    }

    /*public Cursor getAllTraffic()  {

        return db.query(TABLE_THETRAFFIC, new String[] {KEY_ID, KEY_LOCATION}, null, null, null, null, null);
    }*/

    // code to update a single location
    public int updateTraffic(TrafficInfo traffic) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LOCATION, traffic.getLocation());
        //values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        return db.update(TABLE_THETRAFFIC, values, KEY_ID + " = ?",
                new String[] { String.valueOf(traffic.getID()) });
    }

    // Deleting a single location
    public void deleteTraffic(TrafficInfo contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_THETRAFFIC, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
        db.close();
    }

    // Getting the number of locations
    public int getTrafficCount() {
        String countQuery = "SELECT  * FROM " + TABLE_THETRAFFIC;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        //cursor.close();

        // return count
        return cursor.getCount();


    }

}