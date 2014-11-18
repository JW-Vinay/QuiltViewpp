
package com.quiltview.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Xml.Encoding;

import com.quiltview.R;
import com.quiltview.models.QueryModel;
import com.quiltview.networking.HttpConstants.EXECUTION_STATE;
import com.quiltview.networking.HttpConstants.INVOKING_URL;
import com.quiltview.networking.HttpConstants.METHOD_TYPE;
import com.quiltview.utils.LogUtils;
import com.quiltview.utils.Utils;

public class HttpUtils {
    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private QueryResponseInterface mListener;
    private StreamManagementInterface mStreamMgmntListener;
    
    private INVOKING_URL mInvokeCode;

    private METHOD_TYPE mHttpMethod;
    private HashMap<String, String> mHeaderData;
    private HashMap<String, Object> mResponseData;

    /**
     * @param mContext
     */
    public HttpUtils(Context mContext) {
        super();
        this.mContext = mContext;
    }

    /**
     * @param mContext
     * @param mListener
     */
    public HttpUtils(Context mContext, StreamManagementInterface mListener) {
        super();
        this.mContext = mContext;
        this.mStreamMgmntListener = mListener;
        mHeaderData = new HashMap<String, String>();
    }
    
    /**
     * @param mContext
     * @param mListener
     */
    public HttpUtils(Context mContext, QueryResponseInterface mListener) {
        super();
        this.mContext = mContext;
        this.mListener = mListener;
        mHeaderData = new HashMap<String, String>();
    }

    /**
     * Get no of subscribers
     * @param queryId
     */
    public void getNoSubScribers(int queryId)
    {
        String url = HttpConstants.GET_SUBSCRIBERS + "?" + HttpConstants.KEY_QID+"="+queryId;
        this.mHttpMethod = METHOD_TYPE.GET;
        this.mInvokeCode = INVOKING_URL.CHECK_SUBSCRIBERS;
        mHeaderData.put(HttpConstants.KEY_DEVICE_TYPE, Utils.getDeviceId(mContext));
        mHeaderData.put(HttpConstants.KEY_CONTENT_TYPE, HttpConstants.VAL_CONTENT_TYPE);
        initiateAsyncTask(url, false, null);
    }
    
    /**
     * Get Queries
     */
    public void fetchQueries() {
        String url = HttpConstants.GET_QUERIES;
        this.mHttpMethod = METHOD_TYPE.GET;
        this.mInvokeCode = INVOKING_URL.GET_QUERIES;
        mHeaderData.put(HttpConstants.KEY_DEVICE_TYPE, Utils.getDeviceId(mContext));
        initiateAsyncTask(url, false, null);
    }

    /**
     * Post reply to query request
     * 
     * @param queryId
     * @param queryStatus
     */
    public void postQueryResponse(int queryId, boolean queryStatus) {
        String url = HttpConstants.POST_REPLY;
        this.mHttpMethod = METHOD_TYPE.POST;
        this.mInvokeCode = INVOKING_URL.POST_RESPONSE;
        mHeaderData.put(HttpConstants.KEY_DEVICE_TYPE, Utils.getDeviceId(mContext));

        List<NameValuePair> npvp = new ArrayList<NameValuePair>();
        npvp.add(new BasicNameValuePair(HttpConstants.KEY_QUERY_ID, String.valueOf(queryId)));
        npvp.add(new BasicNameValuePair(HttpConstants.KEY_QUERY_STATUS, String.valueOf(queryStatus)));

        initiateAsyncTask(url, false, npvp);
    }

    public void quitStream(int streamId, int queryId) {
        String url = HttpConstants.QUIT_STREAM;
        this.mHttpMethod = METHOD_TYPE.POST;
        this.mInvokeCode = INVOKING_URL.QUIT_STREAM;
        mHeaderData.put(HttpConstants.KEY_DEVICE_TYPE, Utils.getDeviceId(mContext));

        List<NameValuePair> npvp = new ArrayList<NameValuePair>();
        npvp.add(new BasicNameValuePair(HttpConstants.KEY_QUERY_ID, String.valueOf(queryId)));
        npvp.add(new BasicNameValuePair(HttpConstants.KEY_STREAM_ID, String.valueOf(streamId)));

        initiateAsyncTask(url, false, npvp);
    }

    /**
     * create name-value pairs for request
     * 
     * @param url
     * @param showLoader
     * @param npvp
     */
    private void initiateAsyncTask(String url, boolean showLoader, List<NameValuePair> npvp) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new NetworkingTask(url, showLoader).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    npvp);
        else
            new NetworkingTask(url, showLoader).execute(npvp);
    }

    /**
     * Async Task for execution of all networking operations
     * 
     * @author sony
     */
    private class NetworkingTask extends AsyncTask<Object, Void, EXECUTION_STATE> {
        private String url;
        private ProgressDialog dialog;
        private boolean showLoader = false;

        public NetworkingTask(String url, boolean showLoader) {
            this.url = url;
            this.showLoader = showLoader;

            if (showLoader) {
                this.dialog = new ProgressDialog(mContext);
                this.dialog.setIndeterminate(true);
                this.dialog.setCancelable(false);
                this.dialog.setMessage(mContext.getString(R.string.loading_msg));
            }
        }

        @Override
        protected void onPreExecute() {
            try {
                if (showLoader)
                    dialog.show();
            } catch (Exception e) {

            }

        }

        @Override
        protected EXECUTION_STATE doInBackground(Object... params) {
            EXECUTION_STATE state;

            @SuppressWarnings("unchecked")
            List<NameValuePair> npvp = (params != null && params.length > 0) ? (List<NameValuePair>) params[0]
                    : null;

            String response = establishConnection(npvp);

//            System.out.println("response: \n" + response);
            if (response.startsWith("Network Error"))
                state = EXECUTION_STATE.NETWORK_ERROR;
            else {
                state = postNetworkingOperation(response);
            }

            return state;
        }

        @Override
        protected void onPostExecute(EXECUTION_STATE result) {

            super.onPostExecute(result);

            try {
                if (dialog != null && showLoader)
                    dialog.dismiss();
            } catch (IllegalArgumentException e) {
            }

            switch (result) {
                case NETWORK_ERROR:
                    networkErrorCallbackOperations(result);
                    break;

                case RESPONSE_SUCCESS:
                case RESPONSE_FAILURE:
                    responeHandlingOperations(result);
                    break;

                default:
                    defaultHandlingOperations(result);
                    break;
            }
        }

        private Header[] setHeaders()
        {
            Header[] headerArray = new Header[mHeaderData.size()];
            int index = 0;
            for(String key : mHeaderData.keySet())
            {
                
                headerArray[index] = new BasicHeader(key, mHeaderData.get(key));
                index++;
            }
            
            return headerArray;
        }
        
        /**
         * Networking Operations
         * 
         * @param npvp
         * @return
         */
        private String establishConnection(List<NameValuePair> npvp) {
            String response_str = "";
            try {
                HttpParams httpParameters = new BasicHttpParams();
                int timeoutConnection = 10000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                int timeoutSocket = 60000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                HttpClient client = new DefaultHttpClient(httpParameters);
                HttpResponse response = null;
                switch (mHttpMethod) {
                    case POST:
                        HttpPost mPost = new HttpPost(url);
                        mPost.setEntity(new UrlEncodedFormEntity(npvp, Encoding.UTF_8.toString()));
//                        mPost.setHeader(HttpConstants.KEY_DEVICE_TYPE, mHeaderData.get(HttpConstants.KEY_DEVICE_TYPE));
                        mPost.setHeaders(setHeaders());
                        response = client.execute(mPost);
                        break;

                    case GET:
                        HttpGet mGet = new HttpGet(url);
                        URI uri = new URI(url);
                        mGet.setURI(uri);
//                        mGet.setHeader(HttpConstants.KEY_DEVICE_TYPE, mHeaderData.get(HttpConstants.KEY_DEVICE_TYPE));
                        mGet.setHeaders(setHeaders());
                        response = client.execute(mGet);
                        break;

                    case DELETE:
                        break;

                    case PUT:
                        break;

                    default:
                        break;
                }

                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == HttpURLConnection.HTTP_OK
                        || responseCode == HttpURLConnection.HTTP_NO_CONTENT
                        || responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    InputStream is = response.getEntity().getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder str = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        str.append(line + "\n");
                    }
                    is.close();

                    response_str = str.toString();
                } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    response_str = "No Content";
                }
            } catch (ClientProtocolException e) {
                response_str = "Network Error" + e.getMessage();
                LogUtils.LOGE(TAG, response_str);
                // e.printStackTrace();
            } catch (SocketTimeoutException e) {
                response_str = "Network Error" + e.getMessage();
                LogUtils.LOGE(TAG, response_str);
                // e.printStackTrace();
            } catch (ConnectTimeoutException e) {
                response_str = "Network Error" + e.getMessage();
                LogUtils.LOGE(TAG, response_str);
                // e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                response_str = "Network Error" + e.getMessage();
                LogUtils.LOGE(TAG, response_str);
                // e.printStackTrace();
            } catch (Exception e) {
                response_str = "Network Error" + e.getMessage();
                LogUtils.LOGE(TAG, response_str);
                // e.printStackTrace();
            }
            return response_str;
        }

        private void defaultHandlingOperations(EXECUTION_STATE state) {
            String errMsg = mContext.getString(R.string.generic_error_msg);
            switch (mInvokeCode) {
                case GET_QUERIES:
                    mListener.onQueriesReceived(state, null, errMsg);
                    break;
                case POST_RESPONSE:
                    mListener.onPostResponseReceived(state, errMsg, -1);
                    break;
                case CHECK_SUBSCRIBERS:
                    mStreamMgmntListener.onNoSubscribersResponse(state, null, errMsg);
                    break;
                case QUIT_STREAM:
//                    mStreamMgmntListener.onQuitStreamResponse(state, errMsg);
                    break;
                default:
                    break;
            }
        }

        private void networkErrorCallbackOperations(EXECUTION_STATE state) {
            String errorMsg = null;

            errorMsg = mContext.getString(R.string.network_error_msg);
            switch (mInvokeCode) {
                case GET_QUERIES:
                    mListener.onQueriesReceived(state, null, errorMsg);
                    break;
                    
                case POST_RESPONSE:
                    mListener.onPostResponseReceived(state, errorMsg, -1);
                    break;
                    
                case CHECK_SUBSCRIBERS:
                    mStreamMgmntListener.onNoSubscribersResponse(state, null, errorMsg);
                    break;
                    
                case QUIT_STREAM:
//                    mStreamMgmntListener.onQuitStreamResponse(state, errorMsg);
                    break;
                    
                default:
                    break;
            }
        }

        private void responeHandlingOperations(EXECUTION_STATE state) {

            String message = "";
            switch (mInvokeCode) {

                case GET_QUERIES:
                    @SuppressWarnings("unchecked")
                    List<QueryModel> list = (mResponseData != null && mResponseData
                            .get(JsonParser.DATA_SET) != null) ? (List<QueryModel>) mResponseData
                            .get(JsonParser.DATA_SET) : null;
                    message = (mResponseData != null && mResponseData.get(JsonParser.MESSAGE) != null) ? mResponseData
                            .get(JsonParser.MESSAGE).toString() : "";
                    mListener.onQueriesReceived(state, list, message);
                    break;

                case POST_RESPONSE:
                    int id = (mResponseData != null && mResponseData.get(JsonParser.DATA_SET) != null) ? Integer
                            .parseInt(mResponseData.get(JsonParser.DATA_SET).toString()) : -1;
                    message = (mResponseData != null && mResponseData.get(JsonParser.MESSAGE) != null) ? mResponseData
                            .get(JsonParser.MESSAGE).toString() : "";
                    mListener.onPostResponseReceived(state, message, id);
                    break;
                    
                case CHECK_SUBSCRIBERS:
                    QueryModel model = (mResponseData != null && mResponseData.get(JsonParser.DATA_SET) != null) ? (QueryModel)mResponseData.get(JsonParser.DATA_SET): null;
                    message = (mResponseData != null && mResponseData.get(JsonParser.MESSAGE) != null) ? mResponseData
                            .get(JsonParser.MESSAGE).toString() : "";
                    mStreamMgmntListener.onNoSubscribersResponse(state, model, message);        
                    break;
                    
                case QUIT_STREAM:
//                    message = (mResponseData != null && mResponseData.get(JsonParser.MESSAGE) != null) ? mResponseData
//                            .get(JsonParser.MESSAGE).toString() : "";
//                    mStreamMgmntListener.onQuitStreamResponse(state, message);
                    break;
                default:
                    break;
            }
        }

        private EXECUTION_STATE postNetworkingOperation(String response) {
            EXECUTION_STATE state = null;

            switch (mInvokeCode) {
                case GET_QUERIES:
                case POST_RESPONSE:
                case QUIT_STREAM:
                case CHECK_SUBSCRIBERS:
                    state = parseQueriesListAndResponses(response);
                    break;
                default:
                    break;
            }
            return state;
        }
    }

    private EXECUTION_STATE parseQueriesListAndResponses(String response) {
        EXECUTION_STATE mState = EXECUTION_STATE.GENERAL_FAILURE;

        try {
            JsonParser mParser = new JsonParser(mContext);

            if (mInvokeCode == INVOKING_URL.GET_QUERIES)
                mResponseData = mParser.parseQueries(response);
            else if (mInvokeCode == INVOKING_URL.POST_RESPONSE)
                mResponseData = mParser.parseQueriesReponse(response);
            else if (mInvokeCode == INVOKING_URL.QUIT_STREAM)
                mResponseData = mParser.parseQuitStreamResponse(response);
            else if (mInvokeCode == INVOKING_URL.CHECK_SUBSCRIBERS)
                mResponseData = mParser.parseNoSubscriberCountResponse(response);
            
            if (mResponseData != null && mResponseData.size() > 0) {
                int status = Integer.parseInt(mResponseData.get(JsonParser.STATUS).toString());

                mState = (status == 200) ? EXECUTION_STATE.RESPONSE_SUCCESS
                        : EXECUTION_STATE.RESPONSE_FAILURE;
            } else
                mState = EXECUTION_STATE.GENERAL_FAILURE;

        } catch (Exception e) {
            LogUtils.LOGE(TAG, "" + e.getMessage());
            mState = EXECUTION_STATE.PARSER_FAILURE;
        }

        return mState;
    }
}
