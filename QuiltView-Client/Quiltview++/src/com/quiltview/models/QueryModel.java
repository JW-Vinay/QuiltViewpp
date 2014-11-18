
package com.quiltview.models;

public class QueryModel {

    private int mQueryId;
    private String mQueryItem;
    private int mNoActiveStreamers;
    private int mNoActiveWatchers;
    private String mTimeStamp;
    
    /**
     * @param mQueryId
     * @param mQueryItem
     * @param mNoActiveStreamers
     * @param mNoActiveWatchers
     * @param mTimeStamp
     */
    public QueryModel(int mQueryId, String mQueryItem, int mNoActiveStreamers,
            int mNoActiveWatchers, String mTimeStamp) {
        super();
        this.mQueryId = mQueryId;
        this.mQueryItem = mQueryItem;
        this.mNoActiveStreamers = mNoActiveStreamers;
        this.mNoActiveWatchers = mNoActiveWatchers;
        this.setmTimeStamp(mTimeStamp);
    }

    /**
     * @return the mQueryId
     */
    public int getmQueryId() {
        return mQueryId;
    }

    /**
     * @param mQueryId the mQueryId to set
     */
    public void setmQueryId(int mQueryId) {
        this.mQueryId = mQueryId;
    }

    /**
     * @return the mQueryItem
     */
    public String getmQueryItem() {
        return mQueryItem;
    }

    /**
     * @param mQueryItem the mQueryItem to set
     */
    public void setmQueryItem(String mQueryItem) {
        this.mQueryItem = mQueryItem;
    }

    /**
     * @return the mNoActiveStreamers
     */
    public int getmNoActiveStreamers() {
        return mNoActiveStreamers;
    }

    /**
     * @param mNoActiveStreamers the mNoActiveStreamers to set
     */
    public void setmNoActiveStreamers(int mNoActiveStreamers) {
        this.mNoActiveStreamers = mNoActiveStreamers;
    }

    /**
     * @return the mNoActiveWatchers
     */
    public int getmNoActiveWatchers() {
        return mNoActiveWatchers;
    }

    /**
     * @param mNoActiveWatchers the mNoActiveWatchers to set
     */
    public void setmNoActiveWatchers(int mNoActiveWatchers) {
        this.mNoActiveWatchers = mNoActiveWatchers;
    }

    public String getmTimeStamp() {
        return mTimeStamp;
    }

    public void setmTimeStamp(String mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
    }

}
