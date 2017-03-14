package com.tutk.sample_iotcamera;

import java.util.ArrayList;
import java.util.List;

import com.tutk.sample_iotcamera.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

	public static List<MyCamera> cameraList = new ArrayList<MyCamera>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Load Camera parameters from database here.
		MyCamera camera = new MyCamera("Camera", "ELCT81RP8GA7TMPPSFYT", "1234");
		cameraList.add(camera);

		ListView lstCameraList = (ListView) findViewById(R.id.lstCameraList);
		
		lstCameraList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] {"Camera"}));
		lstCameraList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {

				Bundle bundle = new Bundle();
				bundle.putInt("cameraIndex", position);

				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(MainActivity.this, LiveviewActivity.class);
				startActivity(intent);
			}
		});
	}
}
