package org.lyreg.fido_uaf_android_demo;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.lyreg.fido_uaf_android_demo.uaf.AndroidClientIntentParameters;
import org.lyreg.fido_uaf_android_demo.uaf.FidoOperation;
import org.lyreg.fido_uaf_android_demo.uaf.IUafClientUtils;
import org.lyreg.fido_uaf_android_demo.uaf.UafClientUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseActivity extends AppCompatActivity {
    private final static String TAG = "BaseActivity";

    private FidoOperation mCurrentUafOperation;

    // The list of FIDO Clients
    private static List<ResolveInfo> mUafClientList = new ArrayList<>();

    // The set of authenticators on the device
    private static Set<String> mAvailableAuthenticatorAaids = new HashSet<>();

    // UAF Client Utils
    private static IUafClientUtils uafClientUtils = null;

    /**
     * Initialise global interfaces which are made available to all activities which derive from this class
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(uafClientUtils == null) {
            uafClientUtils = new UafClientUtils();
        }
    }

    /**
     * Enable settings menu
     * @param menu The options menu in which you place your items
     * @return true for menu to be displayed, otherwise false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * React to user selecting Settings
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Display settings menu activity.
     */
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
    }

    /***
     * This is the generic callback handler.  This method assumes that the callback is for the
     * purpose of receiving the FIDO client response.  The method will examine the response
     * and determine if the response has been successful or not and calls the appropriate
     * method (processUafClientResponse, onActivityResultFailure) depending on whether the
     * call has been successful or not.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Received response from FIDO UAF Client. Process it on success, or display error on failure
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "********************************");
        Log.d(TAG, "***** onActivityResult. requestCode = " + requestCode + ". resultCode = " + resultCode + " *********");
        Log.d(TAG, "********************************");

        try {
            if(requestCode == AndroidClientIntentParameters.requestCode) {
                String uafResponseJson;
                if(resultCode == RESULT_OK) {
                    Log.d(TAG, "RESULT_OK: " + resultCode);
                    uafResponseJson = uafClientUtils.getUafClientResponse(mCurrentUafOperation, data);
                    this.processUafClientResponse(uafResponseJson);
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "RESULT_CANCELED: " + resultCode);
                    if (data == null) {
                        Log.d(TAG, "No intent returned");
                        this.onActivityResultFailure("No intent returned");
                    } else {
                        Log.d(TAG, "Intent returned, process it.");
                        uafResponseJson = uafClientUtils.getUafClientResponse(mCurrentUafOperation, data);
                        this.processUafClientResponse(uafResponseJson);
                    }
                } else {
                    Log.d(TAG, "Unexpected activity result code: " + resultCode);
                    this.onActivityResultFailure("Unexpected activity result code: " + Integer.toString(resultCode));
                }
            }
        } catch(Throwable ex) {
            Log.d(TAG, "UAF Client Activity Error: " + ex.getMessage());
            this.onActivityResultFailure(ex.getMessage());
        }
    }


    /***
     * This is a default implementation of the FIDO activity callback when the FIDO client
     * calls back to this app and the response has been unsuccessful.
     *
     * @param errorMsg
     */
    protected void onActivityResultFailure(String errorMsg) {}

    /***
     * This is a default implementation of the FIDO activity callback when the FIDO client
     * calls back to this app and the response has been successful.
     *
     * @param uafResponseJson
     */
    protected void processUafClientResponse(String uafResponseJson) {}

    protected static List<ResolveInfo> getUafClientList() {
        return mUafClientList;
    }

    protected void setCurrentUafOperation(FidoOperation operation) {
        this.mCurrentUafOperation = operation;
    }

    protected IUafClientUtils getUafClientUtils() {
        if(uafClientUtils != null) {
            return uafClientUtils;
        } else {
            return new UafClientUtils();
        }
    }

    protected Set<String> getAvailableAuthenticatorAaids() {
        return mAvailableAuthenticatorAaids;
    }
}
