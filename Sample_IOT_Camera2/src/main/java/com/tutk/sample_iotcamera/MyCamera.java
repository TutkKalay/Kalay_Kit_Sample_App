package com.tutk.sample_iotcamera;

public class MyCamera extends com.tutk.IOTC.Camera {

	private String mName;
	private String mUID;
	private String mPwd;

	public MyCamera(String name, String uid, String pwd) {
		mName = name;
		mUID = uid;
		mPwd = pwd;
	}

	@Override
	public void connect(String uid, String pwd) {
		super.connect(uid, pwd);
		mUID = uid;
	}

	@Override
	public void connect(String uid) {
		super.connect(uid);
		mUID = uid;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getUID() {
		return mUID;
	}

	public String getPassword() {
		return mPwd;
	}
}
