package com.example.stepcounterapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BTService extends Service {
    private BluetoothManager btManager;
    private BluetoothAdapter btadapter;
    private BluetoothGatt btGatt;
    private String deviceAddress;
    private final String tag = BTService.class.getSimpleName();

    private static final int stepCountUpdate = 1;
    public static final String stepCountUpdateString = "Step Count Update";

    private final static UUID configDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private final static UUID rcsService = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
    private final static UUID rcsMeasurment = UUID.fromString("00002a53-0000-1000-8000-00805f9b34fb");
    private final static UUID stepCounterBasedCount = UUID.fromString("00001068-0000-1000-8000-00805f9b34fb");
    private final static UUID batteryService = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private final static UUID stepService = UUID.fromString("0000feea-0000-1000-8000-00805f9b34fb");
    private final static UUID stepChar = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb");




    private void broadcastUpdate(String action,BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);

        if (action.equals(stepCountUpdateString)) {
            int steps = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            int calories = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 6);
            int distance = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 3);

            intent.putExtra("Steps", steps);
            intent.putExtra("Calories", calories);
            intent.putExtra("Distance", distance);
        }else {
            System.out.println("error");
        }

        sendBroadcast(intent);
    }


    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        //runs when the connection state changes i.e. connects disconnects or fails
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(tag, "Gatt server connection successful");

                btGatt.discoverServices();
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(tag, "Gatt server disconnected");
                Intent intent = new Intent("btDisconnected");
                sendBroadcast(intent);
            }
            //todo what happens when the connection fails
        }

        //runs when the gatt server successfully finishes discovering services(might be when they are discovered but it gets them all at once)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattCharacteristic characteristic;
            characteristic = gatt.getService(stepService).getCharacteristic(stepChar);
            //local notifications
            gatt.setCharacteristicNotification(characteristic, true);
            //remote notifications
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(configDescriptor);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
            //read for first step count
            gatt.readCharacteristic(characteristic);
        }
        //runs when a characteristic is read by the client
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (characteristic.getUuid().equals(stepChar)) {
                broadcastUpdate(stepCountUpdateString, characteristic);
            }
        }

        //runs when a characteristic that is set to notify changes(server calls client)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(characteristic.getUuid().equals(stepChar)){
                broadcastUpdate(stepCountUpdateString, characteristic);
            }
        }
    };


    public class LocalBinder extends Binder {
        BTService getService() {
            return BTService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        if (btGatt != null) {
            btGatt.disconnect();
            btGatt = null;
        }
        return super.onUnbind(intent);
    }


    private final IBinder binder = new LocalBinder();

    public void start(BluetoothManager manager) {
        if (btManager == null) {
            btManager = manager;
            btadapter = btManager.getAdapter();
        }
    }
    public Boolean needStart() {
        if (btManager == null) {
            return true;
        }else {
            return false;
        }
    }

    public void gattConnect(String address) {
        if (address != null) {
            BluetoothDevice device = btadapter.getRemoteDevice(address);
            if(device != null) {
                btGatt = device.connectGatt(this, true, gattCallback);
            }
        }
    }

    public void gattDiscennect() {
        btGatt.disconnect();
    }

    public void close() {
        btGatt.close();
        btGatt = null;
    }

    public void setDeviceAddress(String address) {
        deviceAddress = address;
    }
    public String getDeviceAddress() {
        return deviceAddress;
    }

    public Boolean isGattConnected() {
        if (btGatt == null) {
            return false;
        } else {
            return true;
        }
    }
}
