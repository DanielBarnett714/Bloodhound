package me.dbarnett.bloodhound;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import me.dbarnett.bloodhound.DB.TrackingEventCollection;
import me.dbarnett.bloodhound.DB.TrackingHistoryAdapter;

/**
 * The type Tracking history activity.
 */
public class TrackingHistoryActivity extends AppCompatActivity {
    /**
     * The Tracking history view.
     */
    RecyclerView trackingHistoryView;
    /**
     * The Tracking event collection.
     */
    TrackingEventCollection trackingEventCollection;
    /**
     * The M adapter.
     */
    TrackingHistoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_history);

        trackingHistoryView = (RecyclerView) findViewById(R.id.tracking_history);
        trackingEventCollection = new TrackingEventCollection(getApplicationContext());


        mAdapter = new TrackingHistoryAdapter(trackingEventCollection.getTrackingEvents());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        trackingHistoryView.setLayoutManager(mLayoutManager);
        trackingHistoryView.setItemAnimator(new DefaultItemAnimator());
        trackingHistoryView.setAdapter(mAdapter);
    }
}
