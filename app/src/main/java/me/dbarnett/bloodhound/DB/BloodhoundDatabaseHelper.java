package me.dbarnett.bloodhound.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import me.dbarnett.bloodhound.DB.BloodhoundDbSchema.TrackingEvents;


/**
 * The type Bloodhound database helper.
 */
public class BloodhoundDatabaseHelper extends SQLiteOpenHelper {
    /**
     * Instantiates a new Bloodhound database helper.
     *
     * @param context the context
     */
    public BloodhoundDatabaseHelper(Context context) {
        super(context, BloodhoundDbSchema.DATABASE_NAME, null, BloodhoundDbSchema.VERSION );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + BloodhoundDbSchema.TrackingEvents.NAME
                + "(_id integer primary key autoincrement, "
                + TrackingEvents.Cols.ID + ", "
                + TrackingEvents.Cols.START_TIME + ", "
                + TrackingEvents.Cols.END_TIME + ", "
                + TrackingEvents.Cols.TRACKING_TYPE
                + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}