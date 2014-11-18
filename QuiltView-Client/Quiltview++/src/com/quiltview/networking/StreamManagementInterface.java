package com.quiltview.networking;

import com.quiltview.models.QueryModel;
import com.quiltview.networking.HttpConstants.EXECUTION_STATE;

public interface StreamManagementInterface  {

    public void onQuitStreamResponse(EXECUTION_STATE state, String message);
    public void onNoSubscribersResponse(EXECUTION_STATE state, QueryModel model, String message);
}
