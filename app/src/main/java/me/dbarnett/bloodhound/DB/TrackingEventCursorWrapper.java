package me.dbarnett.bloodhound.DB;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.UUID;


/**
 * Created by daniel on 4/4/17.
 */

import me.dbarnett.bloodhound.DB.BloodhoundDbSchema.TrackingEvents;

/**
 * The type Tracking event cursor wrapper.
 */
public class TrackingEventCursorWrapper extends CursorWrapper {

    /**
     * Instantiates a new Tracking event cursor wrapper.
     *
     * @param cursor the cursor
     */
    public TrackingEventCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    /**
     * Gets tracking event.
     *
     * @return the tracking event
     */
    public TrackingEvent getTrackingEvent() {
        UUID id = UUID.fromString(getString(getColumnIndex(TrackingEvents.Cols.ID)));
        String startTime = getString(getColumnIndex(TrackingEvents.Cols.START_TIME));
        String endTime = getString(getColumnIndex(TrackingEvents.Cols.END_TIME));
        String trackingType = getString(getColumnIndex(TrackingEvents.Cols.TRACKING_TYPE));



        TrackingEvent trackingEvent = new TrackingEvent(id);
        trackingEvent.setStartTime(startTime);
        trackingEvent.setEndTime(endTime);
        trackingEvent.setTrackingType(trackingType);


        return trackingEvent;

    }
}

