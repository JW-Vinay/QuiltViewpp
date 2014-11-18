
package com.quiltview.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.glass.widget.CardBuilder;
import com.quiltview.R;
import com.quiltview.TimelineService;

public class SplashActivity extends Activity {
    private View mCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CardBuilder builder = new CardBuilder(this, CardBuilder.Layout.TEXT);
        builder.setText(R.string.tag_welcome_msg);
//        builder.setIcon(R.drawable.google_icon);
        builder.setFootnote(R.string.tag_welocme_info);

        mCardView = builder.getView();
        setContentView(mCardView);

        mCardView.setFocusable(true);
        mCardView.setFocusableInTouchMode(true);
        mCardView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openOptionsMenu();
            }
        });
    }

    @Override
    protected void onResume() {

        mCardView.requestFocus();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
     
        if(TimelineService.isServiceRunning())
            menu.getItem(0).setTitle(R.string.tag_stop);
        else
            menu.getItem(0).setTitle(R.string.tag_start);
        
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggleService:
                if (TimelineService.isServiceRunning())
                    stopService(new Intent(SplashActivity.this, TimelineService.class));
                else
                {
                    startService(new Intent(SplashActivity.this, TimelineService.class));
                    closeOptionsMenu();
                    finish();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
