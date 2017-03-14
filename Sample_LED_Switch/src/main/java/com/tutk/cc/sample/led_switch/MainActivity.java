package com.tutk.cc.sample.led_switch;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tutk.IOTC.P2PTunnelAPIs;
import com.tutk.IOTC.sP2PTunnelSessionInfo;

import java.nio.charset.StandardCharsets;

public class MainActivity extends ActionBarActivity implements P2PTunnelAPIs.IP2PTunnelCallback {

    static final String TAG = "LED_SWITCH";
    static final String USER = "Tutk.com";      // refer to P2PTunnelServer.c
    static final String PWD = "P2P Platform";   // refer to P2PTunnelServer.c

    int localPort = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText edtUID = (EditText)findViewById(R.id.edtUID);
        final Button btnOn = (Button)findViewById(R.id.btnOn);
        final Button btnOff= (Button)findViewById(R.id.btnOff);
        final Button btnConnect = (Button)findViewById(R.id.btnConnect);

        // Load UID from SharedPreference
        SharedPreferences sharedPreferences = getSharedPreferences("Preference", 0);
        String UID = sharedPreferences.getString("UID", "");
        edtUID.setText(UID);

        // Disable these LED switch buttons
        btnOn.setEnabled(false);
        btnOff.setEnabled(false);

        // Initiate the P2PTunnelAPI
        final P2PTunnelAPIs api = new P2PTunnelAPIs(this);
        api.P2PTunnelAgentInitialize(4);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String UID = edtUID.getText().toString();
                if (UID.length() == 20) {
                    // Save UID to SharedPrefernce
                    getSharedPreferences("Preference", 0).edit().putString("UID", UID).commit();

                    // Disable the connect button
                    refreshConnectButtonStatus(false);

                    // Start a new thread to connect to device
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int ret = 0;
                            int[] errFromDevice = new int[1];

                            ret = api.P2PTunnelAgent_Connect(UID, getAuthData(USER, PWD), getAuthDataLength(), errFromDevice);
                            if (ret >= 0) {
                                while (true) {
                                    int idx = api.P2PTunnelAgent_PortMapping(ret, localPort, 8080);
                                    if (idx >= 0) {
                                        Log.d(TAG, "Session connected");
                                        // Enable LED buttons
                                        refreshLEDButtonStatus(true);
                                        break;
                                    } else if (idx == P2PTunnelAPIs.TUNNEL_ER_BIND_LOCAL_SERVICE) {
                                        localPort++;
                                        continue;
                                    } else {
                                        // Enable the connection button
                                        refreshConnectButtonStatus(true);
                                        Log.d(TAG, "Failed to mapping ports");
                                        break;
                                    }
                                }
                            } else if (ret == P2PTunnelAPIs.TUNNEL_ER_AUTH_FAILED) {
                                if (errFromDevice[0] == -888) {
                                    Log.d(TAG, "The auth data is wrong.");
                                }
                                // Enable the connection button
                                refreshConnectButtonStatus(true);
                            } else {
                                // Enable the connection button
                                refreshConnectButtonStatus(true);
                            }
                        }
                    }).start();
                }
            }
        });


        // We use Volley to send http command to the device.
        final RequestQueue queue = Volley.newRequestQueue(this);

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send http request to the WRTnode.
                String url = String.format("http://127.0.0.1:%s/led/on", localPort);
                StringRequest request = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "That didn't work!");
                            }
                        }
                );

                queue.add(request);
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send http request to the WRTnode.
                String url = String.format("http://127.0.0.1:%s/led/off", localPort);
                StringRequest request = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "That didn't work!");
                            }
                        }
                );

                queue.add(request);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int getAuthDataLength() {
        return 128;
    }

    private byte[] getAuthData(String username, String password) {
        /* The authdata structure between device and client:
            typedef struct st_AuthData
            {
                char szUsername[64];
                char szPassword[64];
            } sAuthData;
        */

        byte[] result = new byte[128];
        byte[] acc = username.getBytes(StandardCharsets.US_ASCII);
        byte[] pwd = password.getBytes(StandardCharsets.US_ASCII);

        // copy acc and pwd to result
        System.arraycopy(acc, 0, result, 0, acc.length);
        System.arraycopy(pwd, 0, result, 64, pwd.length);

        return result;
    }

    private void refreshConnectButtonStatus(final boolean status) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Button btnConnect = (Button)findViewById(R.id.btnConnect);
                btnConnect.setEnabled(status);
            }
        });
    }

    private void refreshLEDButtonStatus(final boolean status) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Button btnOn = (Button)findViewById(R.id.btnOn);
                final Button btnOff= (Button)findViewById(R.id.btnOff);
                btnOn.setEnabled(status);
                btnOff.setEnabled(status);
            }
        });
    }


    @Override
    public void onTunnelStatusChanged(int nErrCode, int nSID) {
        if (nErrCode == P2PTunnelAPIs.TUNNEL_ER_DISCONNECTED) {
            refreshConnectButtonStatus(true);
            refreshLEDButtonStatus(false);
        }
    }

    @Override
    public void onTunnelSessionInfoChanged(sP2PTunnelSessionInfo object) {

    }
}
