package me.dbarnett.bloodhound.DB;

/**
 * Created by daniel on 4/4/17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.LinkedList;
import java.util.List;
import me.dbarnett.bloodhound.DB.BloodhoundDbSchema.TrackingEvents;

/**
 * The type Tracking event collection.
 */
public class TrackingEventCollection {
    private static TrackingEventCollection sTrackingEventCollection;

    private final Context mContext;
    private final SQLiteDatabase mDatabase;
    private final List<TrackingEvent> mTrackingEvents;

    /**
     * Instantiates a new Tracking event collection.
     *
     * @param context the context
     */
    public TrackingEventCollection(Context context) {

        mContext = context.getApplicationContext();
        mDatabase = new BloodhoundDatabaseHelper(mContext).getWritableDatabase();
        mTrackingEvents = new LinkedList<>();
    }

    /**
     * Get tracking event collection.
     *
     * @param context the context
     * @return the tracking event collection
     */
    public static synchronized TrackingEventCollection get(Context context) {
        if(sTrackingEventCollection == null) {
            sTrackingEventCollection = new TrackingEventCollection(context);
        }
        return sTrackingEventCollection;
    }

    /**
     * Gets tracking events.
     *
     * @return the tracking events
     */
    public List<TrackingEvent> getTrackingEvents() {
        mTrackingEvents.clear();
        TrackingEventCursorWrapper wrapper = querytrackingEvents(null, null);

        try {
            wrapper.moveToLast();
            while(wrapper.isBeforeFirst() == false) {
                TrackingEvent trackingEvent = wrapper.getTrackingEvent();
                mTrackingEvents.add(trackingEvent);
                wrapper.moveToPrevious();
            }
        }
        finally {
            wrapper.close();
        }

        return mTrackingEvents;
    }

    /**
     * Add tracking event.
     *
     * @param trackingEvent the tracking event
     */
    public void addTrackingEvent(TrackingEvent trackingEvent) {
        ContentValues values = getContentvalues(trackingEvent);
            mDatabase.insert(TrackingEvents.NAME, null, values);

    }


    private static ContentValues getContentvalues(TrackingEvent trackingEvent) {
        ContentValues values = new ContentValues();

        values.put(TrackingEvents.Cols.ID, trackingEvent.getId().toString());
        values.put(TrackingEvents.Cols.START_TIME, trackingEvent.getStartTime());
        values.put(TrackingEvents.Cols.END_TIME, trackingEvent.getEndTime());
        values.put(TrackingEvents.Cols.TRACKING_TYPE, trackingEvent.getTrackingType());


        return values;
    }

    private TrackingEventCursorWrapper querytrackingEvents(String where, String[] args) {
        Cursor cursor = mDatabase.query(
                TrackingEvents.NAME, // table name
                null,
                where,
                args,
                null,
                null,
                null
        );

        return new TrackingEventCursorWrapper(cursor);
    }
}
