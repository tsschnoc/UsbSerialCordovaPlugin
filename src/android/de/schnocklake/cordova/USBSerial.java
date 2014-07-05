package de.schnocklake.cordova;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
// kludgy imports to support 2.9 and 3.0 due to package changes
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
// import org.apache.cordova.CordovaArgs;
// import org.apache.cordova.CordovaPlugin;
// import org.apache.cordova.CallbackContext;
// import org.apache.cordova.PluginResult;
// import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.hoho.android.usbserial.util.SerialInputOutputManager.Listener;

/**
 * PhoneGap Plugin for Serial Communication over Bluetooth
 */
public class USBSerial extends CordovaPlugin {

	// actions
	private static final String LIST = "list";
	private static final String CONNECT = "connect";
	private static final String DISCONNECT = "disconnect";
	private static final String WRITE = "write";
	private static final String AVAILABLE = "available";
	private static final String READ = "read";
	private static final String READ_UNTIL = "readUntil";
	private static final String SUBSCRIBE = "subscribe";
	private static final String UNSUBSCRIBE = "unsubscribe";
	private static final String IS_ENABLED = "isEnabled";
	private static final String IS_CONNECTED = "isConnected";
	private static final String CLEAR = "clear";

	// callbacks
	private CallbackContext connectCallback;
	private CallbackContext dataAvailableCallback;

	// Debugging
	private static final String TAG = "USBSerial";

	// Message types sent from the BluetoothSerialService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	StringBuffer buffer = new StringBuffer();
	private String delimiter;

	@Override
	public boolean execute(String action, CordovaArgs args,
			CallbackContext callbackContext) throws JSONException {

		LOG.d(TAG, "action = " + action);


		boolean validAction = true;

		if (action.equals(LIST)) {

			listBondedDevices(callbackContext);

		} else if (action.equals(CONNECT)) {

			boolean secure = true;
			connect(args, secure, callbackContext);

		} else if (action.equals(DISCONNECT)) {

			connectCallback = null;
			/* TODO USB Disconnect */
			callbackContext.success();

		} else if (action.equals(WRITE)) {
			String data = args.getString(0);

			writeSerial(data, callbackContext);
		} else if (action.equals(AVAILABLE)) {

			callbackContext.success(available());

		} else if (action.equals(READ)) {

			callbackContext.success(read());

		} else if (action.equals(READ_UNTIL)) {

			String interesting = args.getString(0);
			callbackContext.success(readUntil(interesting));

		} else if (action.equals(SUBSCRIBE)) {

			delimiter = args.getString(0);
			dataAvailableCallback = callbackContext;

			PluginResult result = new PluginResult(
					PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result);

		} else if (action.equals(UNSUBSCRIBE)) {

			delimiter = null;
			dataAvailableCallback = null;

			callbackContext.success();

		} else if (action.equals(IS_ENABLED)) {

			callbackContext.success();    

		} else if (action.equals(IS_CONNECTED)) {
/*
			if (bluetoothSerialService.getState() == BluetoothSerialService.STATE_CONNECTED) {
				callbackContext.success();
			} else {
				callbackContext.error("Not connected.");
			}
*/
		} else if (action.equals(CLEAR)) {

			buffer.setLength(0);
			callbackContext.success();

		} else {

			validAction = false;

		}

		return validAction;
	}

	/**
	 * Write on the serial port
	 *
	 * @param data
	 *            the {@link String} representation of the data to be written on
	 *            the port
	 * @param callbackContext
	 *            the cordova {@link CallbackContext}
	 */
	private void writeSerial(final String data,
			final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				Log.d(TAG, data);
				try {
					byte[] buffer = data.getBytes();
					Log.d(TAG, "writeSerialsent: " + data);
					sPort.write(buffer, 1000);
					callbackContext.success("sent bytes:  "
							+ new String(buffer));
				} catch (IOException e) {
					// deal with error
					Log.d(TAG, e.getMessage());
					callbackContext.error(e.getMessage());
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

/*TODO disconnect usb */
	}

	private void listBondedDevices(CallbackContext callbackContext)
			throws JSONException {
		Log.i(TAG, "listBondedDevices");
		UsbManager mUsbManager = (UsbManager) cordova.getActivity()
				.getSystemService(Context.USB_SERVICE);


		Log.i(TAG, "usbDeviceList");
		JSONArray deviceList = new JSONArray();

		List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber()
				.findAllDrivers(mUsbManager);

		/*
		 * HashMap<String, UsbDevice> usbDeviceList = mUsbManager.getDeviceList();
		 * for (String deviceName : usbDeviceList.keySet()) { Log.i(TAG,
		 * "usbDeviceList " + deviceName); UsbDevice device =
		 * usbDeviceList.get(deviceName); JSONObject json = new JSONObject();
		 * json.put("name", deviceName); json.put("address",
		 * Integer.toHexString(device.getVendorId()) + ":" +
		 * Integer.toHexString(device.getProductId())); json.put("id",
		 * Integer.toHexString(device.getVendorId()) + ":" +
		 * Integer.toHexString(device.getProductId())); deviceList.put(json); }
		 */
		for (UsbSerialDriver driver : drivers) {
			UsbDevice device = driver.getDevice();

			for (UsbSerialPort port : driver.getPorts()) {
				Log.i(TAG,
						"usbDeviceList "
								+ Integer.toHexString(device.getVendorId())
								+ ":"
								+ Integer.toHexString(device.getProductId()));
				JSONObject json = new JSONObject();
				json.put("name", driver.getClass());
				json.put("address", Integer.toHexString(device.getVendorId())
						+ ":" + Integer.toHexString(device.getProductId())
						+ "_" + port.getPortNumber());
				json.put("id", Integer.toHexString(device.getVendorId()) + ":"
						+ Integer.toHexString(device.getProductId()));
				deviceList.put(json);
			}
		}

		callbackContext.success(deviceList);
	}

	SerialInputOutputManager mSerialIoManager;
	boolean light;
	protected Listener mListener = new SerialInputOutputManager.Listener() {

		@Override
		public void onRunError(Exception e) {
			Log.d(TAG, "Runner stopped.");
		}

		@Override
		public void onNewData(final byte[] usbReceivedData) {
			Log.d(TAG, "Runner onNewData. usbReceivedData: "
					+ new String(usbReceivedData));

			buffer.append(new String(usbReceivedData));

			String c = delimiter;
			String data = "";
			int index = buffer.indexOf(c, 0);
			while (index > -1) {
				data = buffer.substring(0, index + c.length());
				buffer.delete(0, index + c.length());
				index = buffer.indexOf(c, 0);


				Log.d(TAG, "USB Date to subscriber: "
						+ data);
				if (dataAvailableCallback != null) {
					PluginResult result = new PluginResult(
							PluginResult.Status.OK, new String(data));
					result.setKeepCallback(true);

					dataAvailableCallback.sendPluginResult(result);


				}
			}

		}
	};

	private final ExecutorService mExecutor = Executors
			.newSingleThreadExecutor();

	private void startIoManager() {
		if (sPort != null) {
			Log.i(TAG, "Starting io manager ..");
			mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
			mExecutor.submit(mSerialIoManager);
		}
	}

	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							Log.d(TAG, "permission granted for device "
									+ device);

							openSerial();
						}
					} else {
						Log.d(TAG, "permission denied for device " + device);
						notifyConnectionLost("permission denied for device "
								+ device);
					}
				}
			}
		}
	};

	private void openSerial() {
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				UsbManager mUsbManager = (UsbManager) cordova.getActivity()
						.getSystemService(Context.USB_SERVICE);
				UsbDeviceConnection connection = mUsbManager.openDevice(sPort
						.getDriver().getDevice());
				if (connection != null) {
					// get first port and open it

					try {
						sPort.open(connection);
						// get connection params or the default values
						int baudRate = 9600;
						int dataBits = UsbSerialPort.DATABITS_8;
						int stopBits = UsbSerialPort.STOPBITS_1;
						int parity = UsbSerialPort.PARITY_NONE;
						sPort.setParameters(baudRate, dataBits, stopBits,
								parity);

						startIoManager();

					} catch (IOException e) {
						// deal with error
						Log.d(TAG, e.getMessage());
						notifyConnectionLost(e.getMessage());
					}

					Log.d(TAG, "Serial port opened!");

					notifyConnectionSuccess();
				} else {
					notifyConnectionLost("Cannot connect to the device!");
				}

			}

		});
	}

	private UsbSerialPort sPort;

	private void connect(CordovaArgs args, boolean secure,
			CallbackContext callbackContext) throws JSONException {
		UsbManager mUsbManager = (UsbManager) cordova.getActivity()
				.getSystemService(Context.USB_SERVICE);

		String macAddress = args.getString(0);

		sPort = null;

		List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber()
				.findAllDrivers(mUsbManager);

		for (UsbSerialDriver driver : drivers) {
			UsbDevice device = driver.getDevice();

			for (UsbSerialPort port : driver.getPorts()) {
				if (macAddress.equalsIgnoreCase(Integer.toHexString(device
						.getVendorId())
						+ ":"
						+ Integer.toHexString(device.getProductId())
						+ "_"
						+ port.getPortNumber())) {
					sPort = port;
					break;
				}
			}
		}

		// BluetoothDevice device =
		// bluetoothAdapter.getRemoteDevice(macAddress);

		if (sPort != null) {
			connectCallback = callbackContext;
			// bluetoothSerialService.connect(device, secure);

			PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
					cordova.getActivity(), 0,
					new Intent(ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			cordova.getActivity().registerReceiver(mUsbReceiver, filter);

			mUsbManager.requestPermission(sPort.getDriver().getDevice(),
					mPermissionIntent);

			PluginResult result = new PluginResult(
					PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result);

		} else {
			callbackContext.error("Could not connect to " + macAddress);
		}
	}

	private void notifyConnectionLost(String error) {
		if (connectCallback != null) {
			connectCallback.error(error);
			connectCallback = null;
		}
	}

	private void notifyConnectionSuccess() {
		if (connectCallback != null) {
			PluginResult result = new PluginResult(PluginResult.Status.OK);
			result.setKeepCallback(true);
			connectCallback.sendPluginResult(result);
		}
	}


	private int available() {
		return buffer.length();
	}

	private String read() {
		int length = buffer.length();
		String data = buffer.substring(0, length);
		buffer.delete(0, length);
		return data;
	}

	private String readUntil(String c) {
		String data = "";
		int index = buffer.indexOf(c, 0);
		if (index > -1) {
			data = buffer.substring(0, index + c.length());
			buffer.delete(0, index + c.length());
		}
		return data;
	}
}
