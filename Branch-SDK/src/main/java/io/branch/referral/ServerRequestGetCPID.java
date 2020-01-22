package io.branch.referral;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import io.branch.referral.util.BranchCPID;

public class ServerRequestGetCPID extends ServerRequest {

    private BranchCrossPlatformIdListenerWrapper callback;

    // needed to hotfix a bug
    private class BranchCrossPlatformIdListenerWrapper {
        io.branch.referral.util.BranchCrossPlatformId.BranchCrossPlatformIdListener oldCallbackRef;
        io.branch.referral.ServerRequestGetCPID.BranchCrossPlatformIdListener newCallbackRef;

        BranchCrossPlatformIdListenerWrapper(io.branch.referral.util.BranchCrossPlatformId.BranchCrossPlatformIdListener callback){
            oldCallbackRef = callback;
        }
        BranchCrossPlatformIdListenerWrapper(io.branch.referral.ServerRequestGetCPID.BranchCrossPlatformIdListener callback){
            newCallbackRef = callback;
        }

        void onDataFetched(BranchCPID branchCPID, BranchError error) {
            if (newCallbackRef != null) {
                newCallbackRef.onDataFetched(branchCPID, error);
            } else if (oldCallbackRef != null) {
                oldCallbackRef.onDataFetched(branchCPID, error);
            } else {
                PrefHelper.Debug("Warning! Unexpected state in BranchCrossPlatformIdListenerWrapper.onDataFetched");
            }
        }
    }

    ServerRequestGetCPID(Context context, String requestPath,
                         io.branch.referral.util.BranchCrossPlatformId.BranchCrossPlatformIdListener callback) {
        super(context, requestPath);
        this.callback = new BranchCrossPlatformIdListenerWrapper(callback);
        JSONObject reqBody = new JSONObject();
        try {
            setPost(reqBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateEnvironment(context, reqBody);
    }

    ServerRequestGetCPID(Context context, String requestPath, BranchCrossPlatformIdListener callback) {
        super(context, requestPath);
        this.callback = new BranchCrossPlatformIdListenerWrapper(callback);
        JSONObject reqBody = new JSONObject();
        try {
            setPost(reqBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateEnvironment(context, reqBody);
    }

    @Override
    public boolean handleErrors(Context context) {
        return false;
    }

    @Override
    public void onRequestSucceeded(ServerResponse response, Branch branch) {
        if (response != null) {
            if (callback != null) {
                callback.onDataFetched(new BranchCPID(response.getObject()), null);
            }
        } else {
            callback.onDataFetched(null,
                    new BranchError("Failed to get the Cross Platform IDs",
                            BranchError.ERR_BRANCH_INVALID_REQUEST));
        }
    }

    @Override
    public void handleFailure(int statusCode, String causeMsg) {
        callback.onDataFetched(null,
                new BranchError("Failed to get the Cross Platform IDs",
                        BranchError.ERR_BRANCH_INVALID_REQUEST));
    }

    @Override
    public boolean isGetRequest() {
        return false;
    }

    @Override
    public void clearCallbacks() {
    }

    @Override
    public BRANCH_API_VERSION getBranchRemoteAPIVersion() {
        return BRANCH_API_VERSION.V1_CPID;
    }

    @Override
    protected boolean shouldUpdateLimitFacebookTracking() {
        return true;
    }

    public boolean shouldRetryOnFail() {
        return true; // Branch event need to be retried on failure.
    }

    public interface BranchCrossPlatformIdListener {
        void onDataFetched(BranchCPID branchCPID, BranchError error);
    }
}