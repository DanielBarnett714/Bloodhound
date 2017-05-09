package me.dbarnett.bloodhound.DB;

/**
 * Created by daniel on 4/4/17.
 */
public class BloodhoundDbSchema {
    /**
     * The constant VERSION.
     */
    public static final int VERSION = 1;
    /**
     * The constant DATABASE_NAME.
     */
    public static final String DATABASE_NAME = "bloodhound.db";

    /**
     * The type Tracking events.
     */
    public static final class TrackingEvents {
        /**
         * The constant NAME.
         */
        public static final String NAME = "trackingEvents";

        /**
         * The type Cols.
         */
        public static final class Cols {
            /**
             * The constant ID.
             */
            public static final String ID = "id";
            /**
             * The constant START_TIME.
             */
            public static final String START_TIME = "start_time";
            /**
             * The constant END_TIME.
             */
            public static final String END_TIME = "end_time";
            /**
             * The constant TRACKING_TYPE.
             */
            public static final String TRACKING_TYPE = "tracking_type";
        }
    }
}
