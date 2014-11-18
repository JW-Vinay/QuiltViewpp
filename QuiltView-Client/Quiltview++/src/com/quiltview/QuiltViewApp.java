package com.quiltview;

import java.util.List;

import com.quiltview.models.QueryModel;
import com.quiltview.utils.Constants.ACTION_STATE;

import android.app.Application;

public class QuiltViewApp extends Application
{
    private List<QueryModel> mQueriesData;
    private int mCurrentStreamingId = -1;
    private int mCurrentStreamQueryId = -1;
    private long mQuerySetTimeStamp = 0;
    
    private ACTION_STATE mAppState = ACTION_STATE.NONE;
    
    /**
     * @return the mCurrentStreamingId
     */
    public int getmCurrentStreamingId() {
        return mCurrentStreamingId;
    }

    /**
     * @param mCurrentStreamingId the mCurrentStreamingId to set
     */
    public void setmCurrentStreamingId(int mCurrentStreamingId) {
        this.mCurrentStreamingId = mCurrentStreamingId;
    }

    /**
     * @return the mQueriesData
     */
    public List<QueryModel> getmQueriesData() {
        return mQueriesData;
    }

    /**
     * @param mQueriesData the mQueriesData to set
     */
    public void setmQueriesData(List<QueryModel> mQueriesData) {
        if(this.mQueriesData != null)
            this.mQueriesData.clear();
        this.mQueriesData = mQueriesData;
        setmQuerySetTimeStamp(System.currentTimeMillis());
    }

    @Override
    public void onCreate() 
    {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    public int getmCurrentStreamQueryId() {
        return mCurrentStreamQueryId;
    }

    public void setmCurrentStreamQueryId(int mCurrentStreamQueryId) {
        this.mCurrentStreamQueryId = mCurrentStreamQueryId;
    }

    public long getmQuerySetTimeStamp() {
        return mQuerySetTimeStamp;
    }

    public void setmQuerySetTimeStamp(long mQuerySetTimeStamp) {
        this.mQuerySetTimeStamp = mQuerySetTimeStamp;
    }

    public ACTION_STATE getmAppState() {
        return mAppState;
    }

    public void setmAppState(ACTION_STATE mAppState) {
        this.mAppState = mAppState;
    }
}
