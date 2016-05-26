package com.example.mosta.wakana;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * Created by mosta on 26/05/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "wakana";

    // Table Names
    private static final String TABLE_SAMPLE = "samples";

    // Common column names
    private static final String KEY_ID = "id";

    // NOTES Table - column nmaes
    private static final String KEY_HASH = "hash";
    private static final String KEY_LABEL = "label";


    // Checkin table create statement
    private static final String CREATE_TABLE_SAMPLE = "CREATE TABLE " + TABLE_SAMPLE + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_HASH + " TEXT,"
            + KEY_LABEL + " TEXT" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_SAMPLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMPLE);

        // create new tables
        onCreate(db);
    }

    public long createSample(String label, String hash){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_HASH, hash);
        values.put(KEY_LABEL, label);

        long id = db.insert(TABLE_SAMPLE, null, values);

        return id;
    }

    public HashMap<String, String> getAllSamples(){
        HashMap<String, String> samples = new HashMap<>();

        String query = "SELECT * FROM " + TABLE_SAMPLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        // looping through array
        if (c.moveToFirst()){
            do{
                String hash = c.getString(c.getColumnIndex(KEY_HASH));
                String label = c.getString(c.getColumnIndex(KEY_LABEL));
                // add to hashmap
                samples.put(hash, label);
            }while (c.moveToNext());
        }
        return samples;
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
