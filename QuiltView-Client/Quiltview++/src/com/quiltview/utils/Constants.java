package com.quiltview.utils;

public class Constants {

    public static final String TAG_QUERY_ID = "queryId";
    public static final long SUBSCRIBER_RUNNABLE_DELAY = 30 *1000; //TODO: update to higher value
    public static final long EXPIRY_TIMESTAMP = 2 * 60 * 1000; //TODO: update to higher value
    
    public static final String TAG_ALARM_INTENT = "alarm_intent";
    public static final String TAG_REMOVE_CARD  = "remove_card";

    public static final String TAG_POLLING_INTENT = "poll_service";
    public static final long DELAY_MILIIS = 20 * 1000;
    public static long INITIAL_DELAY_MILIIS = 20 * 1000;
    public static enum ACTION_STATE {
        STREAMING,  BROWSING_QUERIES, MENU_INTERACTION, NONE
    }
}
