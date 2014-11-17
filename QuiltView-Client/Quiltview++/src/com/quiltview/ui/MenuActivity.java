
package com.quiltview.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.quiltview.QuiltViewApp;
import com.quiltview.R;
import com.quiltview.TimelineService;
import com.quiltview.utils.Constants.ACTION_STATE;

public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ((QuiltViewApp)getApplication()).setmAppState(ACTION_STATE.MENU_INTERACTION);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.view:
                Intent intent = new Intent(MenuActivity.this, QuerySelectorActivity.class);
                startActivity(intent);
                closeOptionsMenu();

                break;

            case R.id.stop:
                stopService(new Intent(this, TimelineService.class));
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsMenuClosed(android.view.Menu)
     */
    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
//        Intent intent = new Intent(this, TimelineService.class);
//        startService(intent); //Restart when exiting since it was stopped at that time
        ((QuiltViewApp)getApplication()).setmAppState(ACTION_STATE.NONE);
        finish();
    }
}
