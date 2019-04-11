package com.example.stepcounterapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class bluetoothScreen extends AppCompatActivity {
    private User user;

    CheckBox enablebtButton, enablebtVisablilityButton;
    TextView btName;
    ImageView btSearch;
    ListView deviceList;

    private BluetoothAdapter ba;
    private Set<BluetoothDevice> paredDevice;

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

        ba = BluetoothAdapter.getDefaultAdapter();
        if(ba == null) {
            Toast.makeText(this, "Bluetooth not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        if(ba.isEnabled()) {
            enablebtButton.setChecked(true);
        }
//check then have the ability to toggle the phones bluetooth
        enablebtButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    ba.disable();
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
                
            }
        });
    }

    private void list() {
        paredDevice = ba.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : paredDevice) {
            list.add(bt.getName());
        }
        Toast.makeText(this, "Showing Devices", Toast.LENGTH_SHORT).show();
        ArrayAdapter ad = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(ad);
    }

    public String getLocalBluetoothName() {
        if(ba == null) {
            ba = BluetoothAdapter.getDefaultAdapter();
        }
        String name = ba.getName();
        if(name == null) {
            name = ba.getAddress();
        }
        return name;
    }
}
