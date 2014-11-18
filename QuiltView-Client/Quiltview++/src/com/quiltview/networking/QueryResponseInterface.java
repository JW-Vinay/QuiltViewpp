package com.quiltview.networking;

import java.util.List;

import com.quiltview.models.QueryModel;
import com.quiltview.networking.HttpConstants.EXECUTION_STATE;

public interface QueryResponseInterface {

    public void onQueriesReceived(EXECUTION_STATE state, List<QueryModel> querySet, String message);
    public void onPostResponseReceived(EXECUTION_STATE state, String message, int id);
}
