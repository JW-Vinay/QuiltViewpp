
package com.quiltview.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;
import com.quiltview.QuiltViewApp;
import com.quiltview.R;
import com.quiltview.TimelineService;
import com.quiltview.adapters.QueryScrollAdapter;
import com.quiltview.models.QueryModel;
import com.quiltview.utils.Constants;
import com.quiltview.utils.Constants.ACTION_STATE;
import com.quiltview.utils.Utils;

public class QuerySelectorActivity extends Activity implements OnItemClickListener
         {
    private List<CardBuilder> mCards;
    private List<QueryModel> mQueryData;
    private CardScrollView mQueryCardScrollView;
    private QueryScrollAdapter mAdapter;
    private TextView mEmptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cardscroll_layout);
        ((QuiltViewApp)getApplication()).setmAppState(ACTION_STATE.BROWSING_QUERIES);
        generateCards();
        // mQueryCardScrollView = new CardScrollView(this);
        mQueryCardScrollView = (CardScrollView) findViewById(R.id.cardScrollView);
        mEmptyTextView = (TextView) findViewById(android.R.id.empty);

        mQueryCardScrollView.setOnItemClickListener(this);

        mAdapter = new QueryScrollAdapter(this, mCards);
        mQueryCardScrollView.setAdapter(mAdapter);
        mQueryCardScrollView.activate();
        manageViewVisibilty();
//        /**
//         * Stop timeline manager when viewing queries
//         */
//        stopService(new Intent(this, TimelineService.class));

    }

    private void generateCards() {
        mQueryData = ((QuiltViewApp) getApplication()).getmQueriesData();
        mCards = new ArrayList<CardBuilder>();
        for (QueryModel model : mQueryData) {
            CardBuilder buildObj = new CardBuilder(this, CardBuilder.Layout.TEXT);
            buildObj.setText(model.getmQueryItem());
            buildObj.setFootnote(getString(R.string.tag_info_text));
            buildObj.setTimestamp(getString(R.string.tag_current_timestamp));
            mCards.add(buildObj);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.query_menu, menu);
        return true;

    }

    private void manageViewVisibilty()
    {
        if(mCards.size() == 0)
        {
            mQueryCardScrollView.setVisibility(View.GONE);
            mEmptyTextView.setVisibility(View.VISIBLE);
            //Remove live card
            Intent intent = new Intent(this, TimelineService.class);
            intent.putExtra(Constants.TAG_REMOVE_CARD, true);
            startService(intent);
            
        }
        else
        {
            mQueryCardScrollView.setVisibility(View.VISIBLE);
            mEmptyTextView.setVisibility(View.GONE);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int selectedPosition = mQueryCardScrollView.getSelectedItemPosition();
        int queryId = mQueryData.get(selectedPosition).getmQueryId();

        switch (item.getItemId()) {

            case R.id.dismiss:
                mCards.remove(selectedPosition);
                mAdapter.notifyDataSetChanged();
                ((QuiltViewApp) getApplication()).getmQueriesData().remove(selectedPosition);
                manageViewVisibilty();
                return true;

            case R.id.stream:
                
                long timeStamp = Utils.convertToMilliseconds(mQueryData.get(selectedPosition).getmTimeStamp());
                if(!Utils.isExpired(timeStamp))
                {
                    Intent intent = new Intent(this, StreamingActivity.class);
                    intent.putExtra(Constants.TAG_QUERY_ID, queryId);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(this, getString(R.string.tag_query_invalid_msg), Toast.LENGTH_SHORT).show();
                    mCards.remove(selectedPosition);
                    mAdapter.notifyDataSetChanged();
                    ((QuiltViewApp) getApplication()).getmQueriesData().remove(selectedPosition);
                    manageViewVisibilty();
                    //TODO:Maybe destroy the live card here
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        openOptionsMenu();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((QuiltViewApp)getApplication()).setmAppState(ACTION_STATE.NONE);
    }

}
