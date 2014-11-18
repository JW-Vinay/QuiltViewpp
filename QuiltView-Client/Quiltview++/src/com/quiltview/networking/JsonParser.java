
package com.quiltview.networking;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.quiltview.R;
import com.quiltview.models.QueryModel;
import com.quiltview.utils.LogUtils;

public class JsonParser {

    private final String TAG = getClass().getSimpleName();

    private Context mContext;

    private final String TAG_COUNT = "count";
    private final String TAG_QUERIES = "queries";
    private final String TAG_STATUS = "status";
    private final String TAG_MESSAGE = "message";

    private final String TAG_QUERY_ID = "query_id";
    private final String TAG_QUERY_ITEM = "query_item";
    private final String TAG_NO_SUBSCRIBERS = "no_subscribers";
    private final String TAG_NO_ACTIVE_STREAMERS = "no_active_streamers";
    private final String TAG_TIMESTAMP = "timestamp";

    private final String TAG_ID = "id";

    private final String TAG_DATA = "data";
    
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String DATA_SET = "data";

    /**
     * @param mContext
     */
    public JsonParser(Context mContext) {
        super();
        this.mContext = mContext;
    }

    /**
     * parse no of subscribers to stream
     * @param response
     * @return
     */
    public HashMap<String, Object> parseNoSubscriberCountResponse(String response) {
        HashMap<String, Object> data = new HashMap<String, Object>();

        System.out.println("response: \n" + response);
        try {

            JSONObject jObject = new JSONObject(response);
            if (jObject != null) {
                int status = (!jObject.isNull(TAG_STATUS)) ? jObject.getInt(TAG_STATUS) : 0;
                String message = (!jObject.isNull(TAG_MESSAGE)) ? jObject.getString(TAG_MESSAGE)
                        : "";
                data.put(STATUS, status);
                if(status == HttpURLConnection.HTTP_OK)
                {
                    JSONArray jArray = jObject.getJSONArray(TAG_DATA);
                    if(jArray != null && jArray.length() > 0)
                    {
                        JSONObject obj = jArray.getJSONObject(0);
                        QueryModel model = parseQueryModel(obj);
                        data.put(DATA_SET, model);
                    }
                }
                else
                    data.put(MESSAGE, message);

            }

        } catch (JSONException e) {
            LogUtils.LOGE(TAG, "" + e.getMessage());
        }

        return data;
    }

    /**
     * Parse quit stream response
     * @param response
     * @return
     */
    public HashMap<String, Object> parseQuitStreamResponse(String response) {
        HashMap<String, Object> data = new HashMap<String, Object>();

        System.out.println("response: \n" + response);
        try {
            if (!TextUtils.isEmpty(response)) {
                JSONObject jObject = new JSONObject(response);
                if (jObject != null) {
                    int status = (!jObject.isNull(TAG_STATUS)) ? jObject.getInt(TAG_STATUS) : 0;
                    String message = (!jObject.isNull(TAG_MESSAGE)) ? jObject
                            .getString(TAG_MESSAGE) : "";
                    data.put(STATUS, status);
                    data.put(MESSAGE, message);

                }
            } else {
                data.put(STATUS, 200);
                data.put(MESSAGE, mContext.getString(R.string.tag_quit_stream));
            }

        } catch (JSONException e) {
            LogUtils.LOGE(TAG, "" + e.getMessage());
        }

        return data;
    }

    /**
     * Parse post-reply response
     * 
     * @param response
     * @return
     */
    public HashMap<String, Object> parseQueriesReponse(String response) {
        HashMap<String, Object> data = new HashMap<String, Object>();

        try {
            if (!TextUtils.isEmpty(response)) {
                JSONObject jObject = new JSONObject(response);
                if (jObject != null) {
                    int status = (!jObject.isNull(TAG_STATUS)) ? jObject.getInt(TAG_STATUS) : 0;
                    String message = (!jObject.isNull(TAG_MESSAGE)) ? jObject
                            .getString(TAG_MESSAGE) : "";
                    data.put(STATUS, status);
                    data.put(MESSAGE, message);

                    if (status == HttpURLConnection.HTTP_OK) {
                        int id = (!jObject.isNull(TAG_ID)) ? jObject.getInt(TAG_ID) : 0;
                        data.put(DATA_SET, id);
                    }
                }
            } else {
                data.put(STATUS, -1);
                data.put(MESSAGE, mContext.getString(R.string.tag_empty_response));
            }

        } catch (JSONException e) {
            LogUtils.LOGE(TAG, "" + e.getMessage());
        }

        return data;
    }

    /**
     * Parse fetch queries response
     * 
     * @param response
     * @return
     */
    public HashMap<String, Object> parseQueries(String response) {
        HashMap<String, Object> data = new HashMap<String, Object>();

        try {
            JSONObject jObject = new JSONObject(response);

            if (jObject != null) {
                int count = (!jObject.isNull(TAG_COUNT)) ? jObject.getInt(TAG_COUNT) : 0;
                int status = (!jObject.isNull(TAG_STATUS)) ? jObject.getInt(TAG_STATUS) : 0;

                data.put(STATUS, status);

                if (count > 0) {
                    JSONArray jArray = jObject.getJSONArray(TAG_QUERIES);

                    if (jArray != null) {
                        List<QueryModel> queryList = new ArrayList<QueryModel>();

                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject obj = jArray.getJSONObject(i);

                            if (obj != null) {

                                queryList.add(parseQueryModel(obj));
                            }
                        }

                        if (!queryList.isEmpty())
                            data.put(DATA_SET, queryList);
                    } else {
                        String message = (!jObject.isNull(TAG_MESSAGE)) ? jObject
                                .getString(TAG_MESSAGE) : "";
                        data.put(MESSAGE, message);
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.LOGE(TAG, "" + e.getMessage());

        }

        return data;
    }

    private QueryModel parseQueryModel(JSONObject object) throws JSONException {
        int queryId = (!object.isNull(TAG_QUERY_ID)) ? object.getInt(TAG_QUERY_ID) : 0;
        int activeStreamerCount = (!object.isNull(TAG_NO_ACTIVE_STREAMERS)) ? object
                .getInt(TAG_NO_ACTIVE_STREAMERS) : 0;
        int subscriberCount = (!object.isNull(TAG_NO_SUBSCRIBERS)) ? object
                .getInt(TAG_NO_SUBSCRIBERS) : 0;
                
        String query = (!object.isNull(TAG_QUERY_ITEM)) ? object.getString(TAG_QUERY_ITEM) : "";
        
        String timeStamp = (!object.isNull(TAG_TIMESTAMP))? object.getString(TAG_TIMESTAMP) : "";
        QueryModel model = new QueryModel(queryId, query, activeStreamerCount, subscriberCount, timeStamp);

        return model;
    }
}
