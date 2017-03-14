/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gcm.play.android.samples.com.gcmquickstart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    public static final String APP_ID = "com.tutk.cc.samples.tpns";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;

    private EditText mUniqueIDEditText;
    private Button mBindButton;
    private ListView mUniqueIDListView;

    private ArrayList<String> UIDs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUIDsFromPreference();
        syncUniqueID2TPNS();

        mUniqueIDEditText = (EditText) findViewById(R.id.uniqueIDEditText);
        mUniqueIDEditText.setTextColor(Color.WHITE);
        mUniqueIDEditText.setHintTextColor(Color.WHITE);

        mBindButton = (Button) findViewById(R.id.bindButton);

        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                    mUniqueIDEditText.setVisibility(View.VISIBLE);
                    mBindButton.setVisibility(View.VISIBLE);
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, UIDs);
        mUniqueIDListView = (ListView) findViewById(R.id.uniqueIDListView);
        mUniqueIDListView.setClickable(false);
        mUniqueIDListView.setLongClickable(true);
        mUniqueIDListView.setAdapter(adapter);
        mUniqueIDListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UIDs.remove(i);
                        saveUIDsToPreference();
                        adapter.notifyDataSetChanged();
                        syncUniqueID2TPNS();
                    }
                });
                return false;
            }
        });


        mBindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = mUniqueIDEditText.getText().toString();
                if (uid.length() == 20) {
                    UIDs.add(uid);
                    saveUIDsToPreference();
                    syncUniqueID2TPNS();
                    adapter.notifyDataSetChanged();
                } else {

                }
            }
        });

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void syncUniqueID2TPNS() {
        String map = Base64.encodeToString(Utils.generateUIDJsonText(UIDs.toArray(new String[UIDs.size()])).getBytes(), Base64.NO_WRAP);
        String url = String.format("http://push.tutk.com/tpns?cmd=mapsync&os=android&appid=%s&udid=%s&map=%s", APP_ID, Utils.generateUDID(MainActivity.this), map);

        // Request a string response from the TPNS
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the response string.
                        Log.i(TAG, "Cmd: mapsync; Response is: " + response);

                        Toast.makeText(MainActivity.this, "Bind success...", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void saveUIDsToPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            Set<String> set = new HashSet<String>();
            set.addAll(UIDs);
            sharedPreferences.edit().putStringSet("UIDs", set).commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i(TAG, "Success to save " + UIDs.size() + " UID(s) to sharedPreference");
        }
    }

    private void loadUIDsFromPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            Set<String> set = sharedPreferences.getStringSet("UIDs", null);
            UIDs.clear();
            if (set != null) {
                Collections.addAll(UIDs, set.toArray(new String[set.size()]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i(TAG, "Success to load " + UIDs.size() + " UID(s) from sharedPreference");
        }
    }
}
