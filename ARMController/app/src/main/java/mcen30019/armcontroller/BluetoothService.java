/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mcen30019.armcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService {

    private static final String TAG = "BluetoothService";

    private final String appName;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Context mContext;
    private final BluetoothDevice mBluetoothDevice;
    private ConnectThread mConnectThread;
    //private ConnectedThread mConnectedThread;

    private final UUID UUID;

    //tmp can delete
    public boolean rfCommSuccess = false;
    public boolean socketConnectSuccess = false;

    public BluetoothService(BluetoothDevice mBluetoothDevice, BluetoothAdapter mBluetoothAdapter, Context context) {
        this.mBluetoothDevice = mBluetoothDevice;
        appName = context.getString(R.string.app_name);
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = context;
        this.UUID = mBluetoothDevice.getUuids()[0].getUuid();
    }

    public boolean establishConnection(){

        mConnectThread = new ConnectThread(mBluetoothDevice);

        if(rfCommSuccess) {
            Log.d(TAG, "rfCommSucess");
            mConnectThread.start();
        }
        else{
            Log.d(TAG, "rfCommFailed");
            return true;
        }
        while (mConnectThread.isAlive()){
        }
        if (socketConnectSuccess){
            Log.d(TAG, "socket success");
            return true;
        }
        else {
            Log.d(TAG, "socket failed");
            return false;
        }
    }

    private class ConnectThread extends Thread{
        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice btDevice;
        public ConnectThread(BluetoothDevice btDevice){
            this.btDevice = btDevice;
            try {
                bluetoothSocket = btDevice.createRfcommSocketToServiceRecord(UUID);
                Log.d(TAG, "Trying to create a socket to connect to device! UUID " + UUID.toString());
                rfCommSuccess = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "Couldn't create a socket");
                e.printStackTrace();
                rfCommSuccess = false;
            }
        }

        @Override
        public void run(){
            System.out.println("running");
            try {
                bluetoothSocket.connect();
                socketConnectSuccess = true;
            } catch (IOException e) {
                socketConnectSuccess = false;
                e.printStackTrace();
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
