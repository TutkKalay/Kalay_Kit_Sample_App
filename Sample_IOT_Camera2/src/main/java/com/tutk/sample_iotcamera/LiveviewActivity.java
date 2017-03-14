package com.tutk.sample_iotcamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;

import com.tutk.IOTC.Camera;
import com.tutk.sample_iotcamera.R;

public class LiveviewActivity extends Activity implements com.tutk.IOTC.IRegisterIOTCListener {

	com.tutk.IOTC.Monitor monitor;
	MyCamera camera;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_view);

		Bundle bundle = this.getIntent().getExtras();
		int idx = bundle.getInt("cameraIndex");

		// Get camera instance from camera list.
		camera = MainActivity.cameraList.get(idx);

		// Register for listening camera events.
		camera.registerIOTCListener(this);
		
		// Get your monitor view
		monitor = (com.tutk.IOTC.Monitor) this.findViewById(R.id.monitor);
		
		// Attach this camera to monitor to show.
		monitor.attachCamera(camera, 0);

		// Init and connect to camera
		Camera.init(); // initialize IOTCAPIs
		camera.connect(camera.getUID());
		camera.start(0, "admin", camera.getPassword());
		camera.startShow(0);
		camera.startListening(0);

		// Because of AEC, we recommend you opening neither speaking nor listening function.
		// camera.startSpeaking(0);



		// If you wanna send some IOCtrls to camera, you may call camera.sendIOCtrl(...) after starting camera.
		// ex. camera.sendIOCtrl(0, com.tutk.IOTC.AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSTREAMCTRL_REQ, com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlGetStreamCtrlReq.parseContent(0));
	}

	@Override
	public void onPause() {
		super.onPause();

		// Deattach this camera.
		monitor.deattachCamera();

		// Unregister this camera.
		camera.unregisterIOTCListener(this);

		// Stop all actions of camera and then disconnect.
		camera.stopListening(0);
		camera.stopShow(0);
		camera.stop(0);
		camera.disconnect();
		camera = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Uninit IOTCAPIs finally.
		Camera.uninit();
	}

	/* --- Methods implemented below are used for receive information from camera --- */

	public void receiveSessionInfo(final Camera camera, final int status) {

		// The callback function is not from the UI thread, if you want to react with your UI,
		// you MUST attach to the UI thread.

		// Here is code snippet for you to detect current connection status..
		/*
		switch (status) {
		case com.tutk.IOTC.Camera.CONNECTION_STATE_CONNECT_FAILED:
			break;

		case com.tutk.IOTC.Camera.CONNECTION_STATE_CONNECTED:
			break;

		case com.tutk.IOTC.Camera.CONNECTION_STATE_CONNECTING:
			break;

		case com.tutk.IOTC.Camera.CONNECTION_STATE_DISCONNECTED:
			break;

		case com.tutk.IOTC.Camera.CONNECTION_STATE_TIMEOUT:
			break;

		case com.tutk.IOTC.Camera.CONNECTION_STATE_UNKNOWN_DEVICE:
			break;

		case com.tutk.IOTC.Camera.CONNECTION_STATE_UNSUPPORTED:
			break;

		case com.tutk.IOTC.Camera.CONNECTION_STATE_WRONG_PASSWORD:
			break;

		default:
			break;
		}
		*/
	}

	// This function is used for receive AVAPI channel status.
	public void receiveChannelInfo(final Camera camera, final int avChannel, final int status) {

		// The callback function is NOT from the UI thread, if you want to react with your UI, you MUST attach to the UI thread.
	}

	public void receiveFrameData(final Camera camera, final int avChannel, final Bitmap bmp) {

		// The callback function is NOT from the UI thread, if you want to react with your UI, you MUST attach to the UI thread.
	}

	public void receiveFrameInfo(final Camera camera, final int avChannel, final long bitRate, final int frameRate, final int onlineNm, final int frameCount, final int incompleteFrameCount) {

		// The callback function is NOT from the UI thread, if you want to react with your UI, you MUST attach to the UI thread.
	}

	public void receiveIOCtrlData(final Camera camera, final int avChannel, final int avIOCtrlMsgType, final byte[] data) {

		// The callback function is NOT from the UI thread, if you want to react with your UI, you MUST attach to the UI thread.
	}
}
