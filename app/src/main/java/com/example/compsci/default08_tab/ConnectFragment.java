package com.example.compsci.default08_tab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Created by Amanda M on 11/10/15.
 */
public class ConnectFragment extends Fragment implements View.OnClickListener {

	String CC_ROBOTNAME = "";
	TextView cv_statusLabel;
	TextView cv_deviceLabel;
	Button cv_connect;
	Button cv_disconnect;
	ImageView cv_icon;
	ProgressBar cv_batteryBar;
	boolean cv_isConnected;
	int cv_batteryLevel;
	Timer cv_batteryTimer;
	public static final String cv_tag = "MyLog";
	private View rootView;

	// BT Variables
	private BluetoothAdapter cv_btInterface;
	private Set<BluetoothDevice> cv_pairedDevices;
	private BluetoothSocket cv_socket;
	private BluetoothDevice cv_device;

	private BroadcastReceiver cv_btMonitor = null;
	public static InputStream cv_is = null;
	public static OutputStream cv_os = null;

	public static ConnectFragment newInstance(int sectionNumber) {
		ConnectFragment fragment = new ConnectFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public ConnectFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		MainActivity.fragmentPlace = false;
		rootView = inflater.inflate(R.layout.fragment_connect, container, false);
		//text labels
		cv_deviceLabel = (TextView) rootView.findViewById(R.id.xv_deviceLabel);
		cv_statusLabel = (TextView) rootView.findViewById(R.id.xv_statusLabel);
		cv_icon = (ImageView) rootView.findViewById(R.id.xv_logo);

		//connect button
		cv_connect = (Button) rootView.findViewById(R.id.xv_connectButton);
		cv_connect.setOnClickListener(this);

		//disconnect button
		cv_disconnect = (Button) rootView.findViewById(R.id.xv_disconnectButton);
		cv_disconnect.setOnClickListener(this);
		cv_disconnect.setVisibility(View.GONE);

		// set up battery bar
		cv_batteryBar = (ProgressBar) rootView.findViewById(R.id.xv_batteryBar);
		cv_batteryBar.getProgressDrawable().setColorFilter(Color.rgb(0, 90, 0), PorterDuff.Mode.SRC_IN);
		cv_batteryBar.setScaleY(4f);

		//poll battery based on user pref
		setBatteryInterval();

		//monitor bt connection
		setupBTMonitor();
		return rootView;
	}

	private void setBatteryInterval(){
		Boolean batteryInterval = MainActivity.getBatteryPoll();
		if (batteryInterval)
			//poll battery every 1 min
			pollBattery(60000);
		else
			//poll battery every 5 min
			pollBattery(300000);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(cv_btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
		getActivity().registerReceiver(cv_btMonitor, new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
		CC_ROBOTNAME = IntentActivity.CC_ROBOTNAME;
		connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(cv_btMonitor);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.xv_connectButton) {
			getRobotData();
		} else if (v.getId() == R.id.xv_disconnectButton) {
			disconnect();
		}
	}

	//connect via bluetooth
	private void connect() {
		try {
			cv_btInterface = BluetoothAdapter.getDefaultAdapter();
			cv_pairedDevices = cv_btInterface.getBondedDevices();
			Iterator<BluetoothDevice> it = cv_pairedDevices.iterator();
			while (it.hasNext()) {
				cv_device = it.next();
				if (cv_device.getName().equalsIgnoreCase(CC_ROBOTNAME)) {
					try {
						cv_socket = cv_device.createRfcommSocketToServiceRecord(
								java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
						cv_socket.connect();
					} catch (Exception e) {
						cv_statusLabel.setText("Error interacting with remote device");
					}
				}
			}
		} catch (Exception e) {
			cv_statusLabel.setText("Failed to find device");
		}
	}

	//disconnect from robot
	private void disconnect() {
		try {
			//cv_batteryTimer.cancel();
			cv_socket.close();
			cv_is.close();
			cv_os.close();

		} catch (Exception e) {
			cv_statusLabel.setText("Error in disconnect");
		}
	}

	//monitor bluetooth connection
	private void setupBTMonitor() {
		cv_btMonitor = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals("android.bluetooth.device.action.ACL_CONNECTED")) {
					handleConnected();
				}
				if (intent.getAction().equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
					handleDisconnected();
				}
			}
		};
	}

	//set view elements to connected
	private void handleConnected() {
		try {
			cv_is = cv_socket.getInputStream();
			cv_os = cv_socket.getOutputStream();
			cv_isConnected = true;
			cv_connect.setVisibility(View.GONE);
			cv_disconnect.setVisibility(View.VISIBLE);
			cv_icon.setImageResource(R.drawable.bluetoothblue);
			cv_statusLabel.setText("Connected");
			cv_statusLabel.setTextColor(getResources().getColor(R.color.OrangeRed));
		} catch (Exception e) {
			cv_is = null;
			cv_os = null;
		}
	}

	//set view elements to disconnected
	private void handleDisconnected() {
		try {
			cv_isConnected = false;
			cv_connect.setVisibility(View.VISIBLE);
			cv_disconnect.setVisibility(View.GONE);
			cv_deviceLabel.setText("Device");
			cv_icon.setImageResource(R.drawable.bluetoothgray);
			cv_statusLabel.setText("Disconnected");
			cv_statusLabel.setTextColor(getResources().getColor(R.color.Blue));
		} catch (Exception e) {
			cv_is = null;
			cv_os = null;
		}
	}

	//method to handel device info from list intent
	private void getRobotData() {
		Intent lv_intent = new Intent(rootView.getContext(), IntentActivity.class);
		startActivity(lv_intent);
	}

	//poll battery every few minutes
	private void pollBattery(int interval) {
		cv_batteryTimer = new Timer();
		cv_batteryTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (cv_isConnected) {
					cv_batteryLevel = getBatteryLevel();
					//set battery value
					cv_batteryBar.setProgress(cv_batteryLevel);
				}
			}
		}, 0, interval);
	}

	//get battery level as int
	int volt = 100;
	private int getBatteryLevel() {
		try {
			// Command: {0x00, 0x0B}, 0x00 -> response required
			byte[] btMsg = {0x02, 0x00, 0x00, 0x0b};
			byte NXT_COMMAND_GET_BATTERY_LEVEL = btMsg[3];
			cv_os.write(btMsg);

			byte response [] = mReadResponse(NXT_COMMAND_GET_BATTERY_LEVEL);
			if (response == null) {
				Log.e(cv_tag, "No battery level response??");
			} else {
				//for (int i = 0; i < response.length; i++) {
				//Log.i(cv_tag, "\tByte " + i + " == " + response[i]);
				volt =  (response[3] + (response[4] << 8))/100;
				//Log.i("==========", "Battery = " + volt);
				//}
			}
		} catch (Exception e) {
			Log.e(cv_tag, "Error in reading battery level -> " + e.getMessage());
		}
		return volt;
	}
	
	/*
	 * The following code has been retrieved from
	 * UA2E_SenseBot application
	 * written for Unlocking Android, Second Edition
	 * http://manning.com/ableson2
	 * Author: Frank Ableson
	 */
	//read response from robot
	private byte [] mReadResponse(int expectedCommand){
		try {
			// attempt to read two bytes
			int attempts = 0;
			int bytesReady = 0;
			byte [] sizeBuffer = new byte[2];
			while (attempts < 5) {
				bytesReady = cv_is.available();
				if (bytesReady == 0) {
					attempts++;
					Thread.sleep(50);
					Log.i(cv_tag,"Nothing there, let's try again");
				} else {
					Log.i(cv_tag,"There are [" + bytesReady + "] waiting for us!");
					break;
				}
			}
			if (bytesReady < 2) {
				return null;
			}
			int bytesRead = cv_is.read(sizeBuffer,0,2);
			if (bytesRead != 2) {
				return null;
			}
			// calculate response size
			bytesReady = 0;
			bytesReady = sizeBuffer[0] + (sizeBuffer[1] << 8);
			Log.i(cv_tag,"Bytes to read is [" + bytesReady + "]");
			byte [] retBuf = new byte[bytesReady];
			bytesRead = cv_is.read(retBuf);
			if (bytesReady != bytesRead) {
				Log.e(cv_tag,"Unexpected data returned!?");
				return null;
			}
			if (retBuf[1] != expectedCommand) {
				Log.e(cv_tag, "This was an unexpected response");
				return null;
			}
			return retBuf;
		} catch (Exception e) {
			Log.e(cv_tag, "Error in Read Response [" + e.getMessage() + "]");
			return null;
		}
	}
}
