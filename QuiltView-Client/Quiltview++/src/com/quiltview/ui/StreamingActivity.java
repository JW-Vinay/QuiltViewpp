
package com.quiltview.ui;

import java.util.List;

import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.quiltview.QuiltViewApp;
import com.quiltview.R;
import com.quiltview.TimelineService;
import com.quiltview.models.QueryModel;
import com.quiltview.networking.HttpConstants.EXECUTION_STATE;
import com.quiltview.networking.HttpUtils;
import com.quiltview.networking.QueryResponseInterface;
import com.quiltview.networking.StreamManagementInterface;
import com.quiltview.utils.Constants;
import com.quiltview.utils.Constants.ACTION_STATE;

public class StreamingActivity extends Activity implements StreamManagementInterface,
        QueryResponseInterface {

    @SuppressWarnings("unused")
    private final String TAG = getClass().getSimpleName();

    private SurfaceView mSurfaceView;
    private TextView mSubscribersCountTextView;

    private Handler mHandler = new Handler();
    private int mQueryId = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.stream_layout);

        ((QuiltViewApp)getApplication()).setmAppState(ACTION_STATE.STREAMING);
        mQueryId = getIntent().getExtras().getInt(Constants.TAG_QUERY_ID);
        new HttpUtils(StreamingActivity.this, (QueryResponseInterface) StreamingActivity.this)
                .postQueryResponse(mQueryId, true);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mSubscribersCountTextView = (TextView) findViewById(R.id.subscriberCountTextView);

        // Sets the port of the RTSP server to 1234
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(RtspServer.KEY_PORT, String.valueOf(1234));
        editor.commit();

        // Configures the SessionBuilder
        SessionBuilder.getInstance().setSurfaceView(mSurfaceView).setPreviewOrientation(0)
                .setContext(getApplicationContext()).setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setVideoEncoder(SessionBuilder.VIDEO_H264);

        // Starts the RTSP server already started
        this.startService(new Intent(this, RtspServer.class));

        /**
         * Stop timeline manager when streaming attempt is in progress
         */
        stopService(new Intent(this, TimelineService.class));
    }

    private Runnable mCheckSubscriberRunnable = new Runnable() {

        @Override
        public void run() {
            new HttpUtils(StreamingActivity.this,
                    (StreamManagementInterface) StreamingActivity.this).getNoSubScribers(mQueryId);
            
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(mCheckSubscriberRunnable, Constants.SUBSCRIBER_RUNNABLE_DELAY);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.streaming_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.quitStreaming) {
            /**
             * Already being killed in onDestroy
             */
            // stopService(new Intent(this, RtspServer.class));
            // new HttpUtils(this,
            // (StreamManagementInterface)this).quitStream(((QuiltViewApp)
            // getApplication()).getmCurrentStreamingId(), mQueryId);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onQueriesReceived(EXECUTION_STATE state, List<QueryModel> querySet, String message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPostResponseReceived(EXECUTION_STATE state, String message, int id) {
        if (state != EXECUTION_STATE.RESPONSE_SUCCESS) {
            // Stop streaming , return to previous state that is time-line view
            stopService(new Intent(this, RtspServer.class));
            finish();
        } else {
            /**
             * Continue streaming all OK condition
             */
            ((QuiltViewApp) getApplication()).setmCurrentStreamingId(id);
            ((QuiltViewApp) getApplication()).setmCurrentStreamQueryId(mQueryId);
            mHandler.postDelayed(mCheckSubscriberRunnable, Constants.SUBSCRIBER_RUNNABLE_DELAY);
        }

    }

    @Override
    protected void onDestroy() {

        manageAppState();
        super.onDestroy();
    }

    /**
     * Manage streaming and app state, cleanly quit stream
     */
    private void manageAppState() {
        stopService(new Intent(this, RtspServer.class));
        Intent intent = new Intent(this, TimelineService.class);
//        intent.putExtra(Constants.TAG_REMOVE_CARD, true);
        startService(intent); //Restarting timelineservice when streaming is done
        mHandler.removeCallbacksAndMessages(null);
        new HttpUtils(this, (StreamManagementInterface) this).quitStream(
                ((QuiltViewApp) getApplication()).getmCurrentStreamingId(), mQueryId);
        ((QuiltViewApp) getApplication()).setmCurrentStreamingId(-1);
        ((QuiltViewApp) getApplication()).setmCurrentStreamQueryId(-1);
        ((QuiltViewApp)getApplication()).setmAppState(ACTION_STATE.NONE);

    }

    @Override
    public void onQuitStreamResponse(EXECUTION_STATE state, String message) {

        // if(!isDestroyed())
        // finish(); //Clean up on the server itself in case of any
        // failure..Cannot block client
        // if(state == EXECUTION_STATE.RESPONSE_SUCCESS)
        // {
        // finish();
        // }
    }

    @Override
    public void onNoSubscribersResponse(EXECUTION_STATE state, QueryModel model, String message) {

        if (state == EXECUTION_STATE.RESPONSE_SUCCESS && model != null) {
            int noOfSubscribers = model.getmNoActiveWatchers();
            if(noOfSubscribers <0)
                mSubscribersCountTextView.setText(getString(R.string.tag_subscribers) + " "
                        + 0);
            else
                mSubscribersCountTextView.setText(getString(R.string.tag_subscribers) + " "
                    + noOfSubscribers);

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
