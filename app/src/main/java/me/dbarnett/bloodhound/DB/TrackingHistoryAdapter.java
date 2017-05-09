package me.dbarnett.bloodhound.DB;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import me.dbarnett.bloodhound.R;


/**
 * Created by daniel on 4/4/17.
 */
public class TrackingHistoryAdapter extends RecyclerView.Adapter<TrackingHistoryAdapter.MyViewHolder> {

    private List<TrackingEvent> trackingEventsList;

    /**
     * The type My view holder.
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Start time.
         */
        public TextView startTime, /**
         * The End time.
         */
        endTime, /**
         * The Tracking type.
         */
        trackingType;

        /**
         * Instantiates a new My view holder.
         *
         * @param view the view
         */
        public MyViewHolder(View view) {
            super(view);
            startTime = (TextView) view.findViewById(R.id.start_time);
            endTime = (TextView) view.findViewById(R.id.end_time);
            trackingType = (TextView) view.findViewById(R.id.tracking_type);
        }
    }


    /**
     * Instantiates a new Tracking history adapter.
     *
     * @param trackingEventsList the tracking events list
     */
    public TrackingHistoryAdapter(List<TrackingEvent> trackingEventsList) {
        this.trackingEventsList = trackingEventsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tracking_event_row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        TrackingEvent trackingEvent = trackingEventsList.get(position);
        holder.startTime.setText(trackingEvent.getStartTime());
        holder.endTime.setText(trackingEvent.getEndTime());
        holder.trackingType.setText(trackingEvent.getTrackingType());
    }

    @Override
    public int getItemCount() {
        return trackingEventsList.size();
    }
}