package com.quiltview.networking;

public class HttpConstants {

    public static enum METHOD_TYPE
    {
        POST, GET, DELETE, PUT
    }
    
    
    public static enum INVOKING_URL
    {
        GET_QUERIES, POST_RESPONSE, CHECK_SUBSCRIBERS, QUIT_STREAM
    }
    
    public static enum EXECUTION_STATE
    {
        RESPONSE_SUCCESS, RESPONSE_FAILURE, PARSER_FAILURE, NETWORK_ERROR, GENERAL_FAILURE, FILESYSTEM_ERROR
    }
    
    public static final String BASE_URL = "http://vm005.elijah.cs.cmu.edu:5000/";
    public static final String GET_QUERIES = BASE_URL + "get-queries";
    public static final String POST_REPLY = BASE_URL + "post-reply";
    public static final String QUIT_STREAM = BASE_URL + "quit-stream";
    public static final String GET_SUBSCRIBERS = BASE_URL + "count-subscribers";
    
    public static final String KEY_STREAM_ID = "stream_id";
    public static final String KEY_QUERY_ID = "query_id";
    public static final String KEY_QUERY_STATUS = "query_status";
    public static final String KEY_QID = "qid";
    
    public static final String KEY_DEVICE_TYPE = "device_type";
    public static final String KEY_CONTENT_TYPE = "Content-Type";
    public static final String VAL_CONTENT_TYPE = "application/x-www-form-urlencoded";
    
}
