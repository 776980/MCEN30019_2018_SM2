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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
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

    private final String connectionVerificationString = "OK";
    private static final String TAG = "BluetoothService";

    private final Context mContext;
    private final BluetoothDevice mBluetoothDevice;
    private BluetoothSocket bluetoothSocket;

    private final UUID UUID;

    //tmp can delete
    private boolean rfCommSuccess = false;
    private boolean socketConnectSuccess = false;

    BluetoothService(BluetoothDevice mBluetoothDevice, Context context) {
        this.mBluetoothDevice = mBluetoothDevice;
        this.mContext = context;
        this.UUID = mBluetoothDevice.getUuids()[0].getUuid();
    }

    public BluetoothSocket establishConnection(BluetoothDevice device) throws InterruptedException {
        ConnectThread mConnectThread = new ConnectThread(mBluetoothDevice);
        if(rfCommSuccess) {
            Log.d(TAG, "rfCommSucess");
            mConnectThread.start();
        }
        else{
            Log.d(TAG, "rfCommFailed");
            return null;
        }
        mConnectThread.join();
        if (socketConnectSuccess){
            Log.d(TAG, "socket success");
            return bluetoothSocket;
        }
        else {
            Log.d(TAG, "socket failed");
            return null;
        }
    }

    public BluetoothSocket establishConnection() throws InterruptedException {
        return establishConnection(mBluetoothDevice);
    }

    private class ConnectThread extends Thread{
        ConnectThread(BluetoothDevice btDevice) {
            try {
                bluetoothSocket = btDevice.createRfcommSocketToServiceRecord(UUID);
                Log.d(TAG, "Trying to create a socket to connect to device! UUID " + UUID.toString());
                rfCommSuccess = true;
            } catch (IOException e) {
                Log.d(TAG, "Couldn't create a socket");
                e.printStackTrace();
                rfCommSuccess = false;
            }
        }

        @Override
        public void run(){
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

        ConnectedThread(BluetoothSocket socket) {
            this.connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
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
                    strReceived(msgReceived);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void strReceived(String msg) {
        Toast.makeText(mContext, "Received msg " + msg + " ", Toast.LENGTH_LONG);
    }

    public boolean sendMessage(String msg){
        ConnectedThread mConnectedThread = new ConnectedThread(bluetoothSocket);

        mConnectedThread.write(msg.getBytes());

        //mConnectedThread.start();

        if (bluetoothSocket.isConnected()){
            //Toast.makeText(mContext, "Trying to send " + msg, Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    public boolean checkSocketConnection(){
        if (bluetoothSocket == null){
            return false;
        }

        return bluetoothSocket.isConnected();
    }
    public String receiveMessage() throws InterruptedException {
        ConnectedThread mConnectedThread = new ConnectedThread(bluetoothSocket);
        mConnectedThread.write(connectionVerificationString.getBytes());
        wait(2000);
        mConnectedThread.start();
        return null;
    }
}
