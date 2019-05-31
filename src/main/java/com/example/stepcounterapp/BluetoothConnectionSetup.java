package com.example.stepcounterapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BluetoothConnectionSetup extends AppCompatActivity implements BluetoothAdapter.LeScanCallback{
    private User user;
    private BluetoothManager btManager;
    private BluetoothAdapter btadapter;
    private BluetoothGatt btGatt;
    private ArrayList<BluetoothDevice> deviceArray;
    private ArrayList<String> deviceNameArray;
    private final String tag = BluetoothConnectionSetup.class.getSimpleName();
    private BTService btService;

    ListView deviceList;
    TextView connectedDeviceName;
    Button scanButton;

    private static final int stepCountUpdate = 1;

    /*  **/

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BTService.LocalBinder) service).getService();
            if (btService.needStart()) {
                btService.start((BluetoothManager) getSystemService(BLUETOOTH_SERVICE));
            }

            //attempt auto-connect
            if (btService.getDeviceAddress() != null) {
                btService.gattConnect(btService.getDeviceAddress());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection_setup);

        // pass user data from previous screen
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");


        btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        btadapter = btManager.getAdapter();


        deviceArray = new ArrayList<BluetoothDevice>();
        deviceNameArray = new ArrayList<String>();

        //set page elements as variables
        deviceList = findViewById(R.id.deviceList);
        connectedDeviceName = findViewById(R.id.connectedDeviceNameDisplay);
        scanButton = findViewById(R.id.scanButton);

        //bind the service that handles the bluetooth
        Intent gattServiceIntent = new Intent(this, BTService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        // add listener to the scan button on the bluetooth connection screen
        scanButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        // add listener to the items in the device list on the bluetooth connection screen
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //stop the current scan
                handler.removeCallbacks(runnableStopScan);
                stopScan();
                //try to connect to the selected device
                BluetoothDevice device = deviceArray.get(position);
                Log.i(tag, "Attempting to connect to " + device.getName());

                if (device != null) {
                    Intent intent = new Intent(BluetoothConnectionSetup.this, Main.class);
                    intent.putExtra("Device address", device.getAddress());
                    intent.putExtra("userData", user);

                    if (btService != null) {
                        if (!btService.isGattConnected()) {
                            btService.gattConnect(device.getAddress());
                        }
                        btService.setDeviceAddress(device.getAddress());
                        System.out.println("temp");
                    }else {
                        System.out.println("btService not initialised");
                    }
                    startActivity(intent);
                }
                //todo remove device name textbox as it is not used
            }
        });

        //if location access is not given request it
        //todo test permission requesting
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

    }

    //runs when the page is brought to the foreground of the device screen
    @Override
    protected void onResume() {
        super.onResume();

        //todo add this to all onResume callbacks
        //check bluetooth
        if(btadapter == null || !btadapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
            finish();
        }
        //check LE bluetooth
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "Low Energy Bluetooth not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        //rebind the BT service and attempt to connect to gatt server
        registerReceiver(updateReciver, generateIntentFilter());
        if (btService != null) {
            btService.gattConnect(btService.getDeviceAddress());
        }
    }

    //runs when the page is removed from the foreground of the device screen
    @Override
    protected void onPause() {
        //todo add this to all onPause callbacks
        super.onPause();
        unregisterReceiver(updateReciver);
        //stop any unfired callbacks and stop scanning
        handler.removeCallbacks(runnableStartScan);
        handler.removeCallbacks(runnableStopScan);
        btadapter.stopLeScan(this);
    }

    //runs when the activity is closed either trough the app closing or navigating to another screen
    @Override
    protected void onStop() {
        UserDatabase db = new UserDatabase(this);
        db.open();
        db.saveUser(user);
        db.close();

        //todo possibly remove this if decide to move bt code somewhere else
        super.onStop();
        //if still connected to a gatt server disconnect
        if (btGatt != null) {
            btGatt.disconnect();
            btGatt = null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Main.class);
        intent.putExtra("userData", user);
        startActivity(intent);
        finish();
    }

    //runs whenever a device is detected during a LE bluetooth scan
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {//callback for whenever a new device is detected
        Log.i(tag, "New LE bluetooth device found: " + device.getName() + " @ " + device.getAddress());

        //check if the device found has already been listed
        if(!deviceArray.contains(device)){
            //if it has not been listed put it in the device array
            deviceArray.add(device);
            //then check if a name is available and either get it or if it is not available set the default name
            if (device.getName() != null) {
                deviceNameArray.add(device.getName());
            }else {
                deviceNameArray.add("Unknown Device");
            }
        }

        //put the contents of the array into the list on the page
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceNameArray);
        deviceList.setAdapter(arrayAdapter);

    }

    //runnable function calls that can be delayed as required
    //todo might get rid of runnable start method dont think i use it anywhere
    private Runnable runnableStopScan = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable runnableStartScan = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    //starts the LE scan
    private void startScan() {
        //todo cause the scan button to indicate that a scan is happening
        //clear the old scan results
        deviceArray.clear();
        deviceNameArray.clear();
        //start the LE scan
        btadapter.startLeScan(this);
        //set the scanStop() function to run 10 seconds after this function does
        handler.postDelayed(runnableStopScan, 10000);
        //todo add progress bar
    }

    private void stopScan() {
        //todo return scan button to normal
        //stop the LE scan
        btadapter.stopLeScan(this);
        //todo add progress bar
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        //runs when a message is broadcast through the handler
        @Override
        public void handleMessage(Message inputMessage) {
            BluetoothGattCharacteristic characteristic;
            //check the message has contents
            if(inputMessage.obj != null){
                //extract the message contents
                characteristic = (BluetoothGattCharacteristic)inputMessage.obj;

                //depending on when the message says do something
                if (inputMessage.what == stepCountUpdate){
                    if(characteristic.getValue() == null) {
                        Log.i(tag, "Failed to obtain step data");
                        return;
                    }
                    //todo what happens when we get the step data(function call)
                }else {
                    //todo error checking for message contents(remove)
                    System.out.println("handler.what error");
                }
            }

        }
    };

    private final BroadcastReceiver updateReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BTService.stepCountUpdateString)) {
                user.updateSteps(intent.getIntExtra("Steps", 0));
                //todo use step data
                int temp = user.getSteps();
                System.out.println("temp");
            } else {
                System.out.println("broadcast receiver error");
            }
        }
    };

    private static IntentFilter generateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BTService.stepCountUpdateString);

        return intentFilter;
    }
}
