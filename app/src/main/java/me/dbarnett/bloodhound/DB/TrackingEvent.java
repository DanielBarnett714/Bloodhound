package me.dbarnett.bloodhound.DB;

/**
 * Created by daniel on 4/4/17.
 */

import java.util.UUID;


/**
 * The type Tracking event.
 */
public class TrackingEvent {
    private UUID mId;
    private String mStartTime;
    private String mEndTime;
    private String mTrackingType;


    /**
     * Instantiates a new Tracking event.
     */
    public TrackingEvent() {
        this(UUID.randomUUID());
    }

    /**
     * Instantiates a new Tracking event.
     *
     * @param id the id
     */
    public TrackingEvent(UUID id) {
        mId = id;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public UUID getId() {
        return mId;
    }

    /**
     * Gets start time.
     *
     * @return the start time
     */
    public String getStartTime() {
        return mStartTime;
    }

    /**
     * Sets start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    /**
     * Gets end time.
     *
     * @return the end time
     */
    public String getEndTime() {
        return mEndTime;
    }

    /**
     * Sets end time.
     *
     * @param endTime the end time
     */
    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }

    /**
     * Gets tracking type.
     *
     * @return the tracking type
     */
    public String getTrackingType() {
        return mTrackingType;
    }

    /**
     * Sets tracking type.
     *
     * @param trackingType the tracking type
     */
    public void setTrackingType(String trackingType) {
        mTrackingType = trackingType;
    }


    @Override
    public String toString() {
        return "TrackingEvent[start_time=" + getStartTime()
                + ", end_time=" + getEndTime()
                + ", tracking_type=" + getTrackingType()
                + "]";
    }
}
