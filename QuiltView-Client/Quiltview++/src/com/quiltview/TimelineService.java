
package com.quiltview;

/**
 * 
 */
import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.quiltview.models.QueryModel;
import com.quiltview.networking.HttpConstants.EXECUTION_STATE;
import com.quiltview.networking.HttpUtils;
import com.quiltview.networking.QueryResponseInterface;
import com.quiltview.ui.MenuActivity;
import com.quiltview.utils.Constants;
import com.quiltview.utils.Constants.ACTION_STATE;
import com.quiltview.utils.Utils;

/**
 * @author ramkrishnan_v
 */
public class TimelineService extends Service implements QueryResponseInterface {
    @SuppressWarnings("unused")
    private final String TAG = getClass().getSimpleName();

    private Handler mHandler;

    private LiveCard mCard;
    private RemoteViews mLiveCardView;

    private final UpdateLiveCardRunnable mUpdateLiveCardRunnable = new UpdateLiveCardRunnable();

    private static boolean isServiceRunning = false;

    // private long DELAY_MILIIS = 20 * 1000;

    /*
     * (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    /**
     * Create Live Card
     * 
     * @param title
     */
    private void createLiveCard(String title) {
        if (mCard == null) {

            mCard = new LiveCard(this, getString(R.string.app_name));

            mLiveCardView = new RemoteViews(getPackageName(), R.layout.card_layout);

            Intent menuIntent = new Intent(this, MenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            setData(title);
            mCard.publish(PublishMode.REVEAL);

        } else {
            setData(title);
            mCard.navigate();
        }

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.TAP);
    }

    /**
     * Set data to the live card
     * 
     * @param title
     */
    private void setData(String title) {
        mLiveCardView.setTextViewText(R.id.title, title);
        Calendar c = Calendar.getInstance();

        String lastUpdatedTime = getString(R.string.tag_last_updated) + " at "
                + String.format("%tT, %tD", c, c);

        mLiveCardView.setTextViewText(R.id.timestamp, lastUpdatedTime);
        mCard.setViews(mLiveCardView);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = true;

        if (intent.getExtras() != null
                && intent.getExtras().getBoolean(Constants.TAG_ALARM_INTENT, false)) {
            clearData();
        } else if (intent.getExtras() != null
                && intent.getExtras().getBoolean(Constants.TAG_REMOVE_CARD, false)) {

            if (mCard != null && mCard.isPublished()) {

                mCard.unpublish();
                mCard = null;
            }

        } else {
            Utils.setPollingAlarmManager(this);
            mHandler.removeCallbacks(mUpdateLiveCardRunnable);
            mHandler.removeCallbacksAndMessages(null);
            mHandler.post(mUpdateLiveCardRunnable);
        }
        // else

        return START_STICKY;
    }

    /*
     * Remove cards from the timeline
     */
    private void clearData() {

        if (((QuiltViewApp) getApplication()).getmAppState() == ACTION_STATE.NONE) {
            ((QuiltViewApp) getApplication()).setmQuerySetTimeStamp(0);
            ((QuiltViewApp) getApplication()).setmQueriesData(null);

            if (mCard != null && mCard.isPublished()) {

                mCard.unpublish();
                mCard = null;
            }

        } else {
            // To remove them once job/interaction done
            setAlarmManager();
        }

    }

    private class UpdateLiveCardRunnable implements Runnable {
        private boolean mIsStopped = false;

        public boolean isStopped() {
            return mIsStopped;
        }

        public void setStop(boolean isStopped) {
            this.mIsStopped = isStopped;
        }

        public void run() {

            if (!isStopped()) {
                new HttpUtils(TimelineService.this, TimelineService.this).fetchQueries();

            }
        }
    }

    private void setAlarmManager() {
        Intent myIntent = new Intent(this, TimelineService.class);
        myIntent.putExtra(Constants.TAG_ALARM_INTENT, true);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, myIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()
                + Constants.EXPIRY_TIMESTAMP, pendingIntent);
    }

    @Override
    public void onQueriesReceived(EXECUTION_STATE state, List<QueryModel> querySet, String message) {

        if (querySet != null && !querySet.isEmpty()) {
            // System.out.println("TSize: " + querySet.size());
            String title = getString(R.string.tag_query_msg);
            title = title.replaceAll("\\$KEY_QUERY_COUNT\\$", "" + querySet.size());
            createLiveCard(title);

            ((QuiltViewApp) getApplication()).setmQueriesData(querySet);
            setAlarmManager();
        }

        /**
         * Run the handler irrespective of status doing this in the handler
         */
        // mHandler.postDelayed(mUpdateLiveCardRunnable, DELAY_MILIIS);
    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;

        if (mCard != null && mCard.isPublished()) {

            mUpdateLiveCardRunnable.setStop(true);
            Utils.cancelPollingEvent(this);
            mHandler.removeCallbacks(mUpdateLiveCardRunnable);
            mHandler.removeCallbacksAndMessages(null);
            mCard.unpublish();
            mCard = null;
        }
        Utils.cancelPollingEvent(this);
        super.onDestroy();
    }

    @Override
    public void onPostResponseReceived(EXECUTION_STATE state, String message, int id) {

    }

    /**
     * @return the isServiceRunning
     */
    public static boolean isServiceRunning() {
        return isServiceRunning;
    }

    /**
     * @param isServiceRunning the isServiceRunning to set
     */
    public static void setServiceRunning(boolean isServiceRunning) {
        TimelineService.isServiceRunning = isServiceRunning;
    }

}
