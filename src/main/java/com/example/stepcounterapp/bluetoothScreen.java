package com.example.stepcounterapp;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class bluetoothScreen extends AppCompatActivity {
    private User user;


    CheckBox enablebtButton, enablebtVisablilityButton;
    TextView btName;
    ImageView btSearch;
    ListView deviceList;



    private BluetoothAdapter btAdaptor;
    private BluetoothSocket btSocket;
    private Set<BluetoothDevice> paredDevice;
    private BluetoothDevice watch;
    private final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_screen);
        Intent i = getIntent();
        user = (User)i.getSerializableExtra("userData");

        enablebtButton = findViewById(R.id.enableBluetoothButton);
        enablebtVisablilityButton = findViewById(R.id.enableBluetoothVisablilityButton);
        btName = findViewById(R.id.bluetoothDeviceName);
        deviceList = findViewById(R.id.deviceList);
        btSearch = findViewById(R.id.bluetoothSearchButton);

        btName.setText(getLocalBluetoothName());
//check if the phone has a bluetooth transmitter
        btAdaptor = BluetoothAdapter.getDefaultAdapter();
        if(btAdaptor == null) {
            Toast.makeText(this, "Bluetooth not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        if(btAdaptor.isEnabled()) {
            enablebtButton.setChecked(true);
        }
//check then have the ability to toggle the phones bluetooth
        enablebtButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    btAdaptor.disable();
                    Toast.makeText(bluetoothScreen.this, "Turned Off", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intentOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intentOn, 0);
                    Toast.makeText(bluetoothScreen.this, "Turned On", Toast.LENGTH_SHORT).show();
                }
            }
        });
//change whether the phones bluetooth is discoverable or not
        enablebtVisablilityButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Intent getVis = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(getVis, 0);
                    Toast.makeText(bluetoothScreen.this, "Visable For 2 Minutes", Toast.LENGTH_SHORT).show();
                }
            }
        });
// when the bluetooth image is pressed list paired devices
        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               list();
            }
        });
//todo when one of the paired devices is selected
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String btDeviceName = deviceList.getItemAtPosition(position).toString();
                String btDeviceAddress;
                for(BluetoothDevice bt : paredDevice) {
                    String temp = bt.getName();
                    if(btDeviceName.compareTo(bt.getName()) == 0) {
                        btDeviceAddress = bt.getAddress();
                        watch = btAdaptor.getRemoteDevice(btDeviceAddress);
                        break;
                    }
                }
                try {
                    btSocket = watch.createRfcommSocketToServiceRecord(myUUID);
                    btAdaptor.cancelDiscovery();
                    

                } catch (IOException e) {
                    e.printStackTrace();
                }
                getSteps(btSocket);
            }
        });
    }

    private void getSteps(BluetoothSocket socket) {
//todo get and set the steps taken
        try {
            socket.connect();
            System.out.println("pause");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void list() {
        paredDevice = btAdaptor.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : paredDevice) {
            list.add(bt.getName());
        }
        Toast.makeText(this, "Showing Devices", Toast.LENGTH_SHORT).show();
        ArrayAdapter ad = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(ad);
    }



    public String getLocalBluetoothName() {
            if (btAdaptor == null) {
                btAdaptor = BluetoothAdapter.getDefaultAdapter();
            }
            String name = btAdaptor.getName();
            if (name == null) {
                name = btAdaptor.getAddress();
            }
            return name;
}

}
/*class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //todo manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}**/
