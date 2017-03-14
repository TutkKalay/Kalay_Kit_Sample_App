package com.tutk.cc.sample.iotc_basics;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tutk.IOTC.IOTCAPIs;

public class MainActivity extends AppCompatActivity {

    EditText edtUID;
    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUID = (EditText)this.findViewById(R.id.edtUID);
        btnConnect = (Button)this.findViewById(R.id.btnConnect);

        IOTCAPIs.IOTC_Initialize2(0);

        btnConnect.setOnClickListener(btnConnectClickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IOTCAPIs.IOTC_DeInitialize();
    }

    View.OnClickListener btnConnectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String UID = edtUID.getText().toString();
            if (UID.length() == 20) {
                int sessionID = IOTCAPIs.IOTC_Connect_ByUID(UID);
                if (sessionID >= 0) {
                    Toast.makeText(MainActivity.this, "Success: " + sessionID, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + sessionID, Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}
