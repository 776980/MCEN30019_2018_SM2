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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private final Context mContext;
    private final BluetoothDevice mBluetoothDevice;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private BluetoothSocket bluetoothSocket;

    private final UUID UUID;

    //tmp can delete
    public boolean rfCommSuccess = false;
    public boolean socketConnectSuccess = false;

    public BluetoothService(BluetoothDevice mBluetoothDevice, Context context) {
        this.mBluetoothDevice = mBluetoothDevice;
        appName = context.getString(R.string.app_name);
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
    private class ConnectedThread extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.inputStream = in;
            this.outputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);
                    final String msgReceived = String.valueOf(bytes) +
                            " bytes received:\n"
                            + strReceived;
                    Toast.makeText(mContext, "RECIEVED MESSAGE: "+ msgReceived, Toast.LENGTH_LONG);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public boolean sendMessage(String msg){
        mConnectedThread = new ConnectedThread(bluetoothSocket);
        mConnectedThread.start();
        mConnectedThread.write(msg.getBytes());
        return false;
    }
}
