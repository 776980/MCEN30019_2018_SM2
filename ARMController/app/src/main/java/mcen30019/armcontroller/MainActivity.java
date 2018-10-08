package mcen30019.armcontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import javax.net.ssl.SNIHostName;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.io.IOException;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private boolean preliminaryTaskStatus = false;
    private final String bluetoothName = "HC-06";
    private BlueToothStatus currentStatus = BlueToothStatus.CONNECTING;
    private ImageView connectionStatusIcon;
    private BluetoothService mBluetoothService;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Button testConnectionButton;


    private int REQUEST_ENABLE_BT = 1;

    private boolean doPreliminaryTasks() {
        //check what task was being done after coming to the app
        if (getBluetoothPermissions()) {
            if (turnBluetoothOn()) {
                if(checkBluetoothPairing()){
                    if (connectBluetooth()) {
                        if (testConnection()) {
                            //Everything okay
                            preliminaryTaskStatus = true;
                            return true;
                        }
                    }
            }   }
        }
        return false;
    }

    public void onResume(){
        super.onResume();
        if (!preliminaryTaskStatus){
            doPreliminaryTasks();
        };
        //Do your work
    }

    private enum BlueToothStatus{
        CONNECTING,
        CONNECTED,
        ERROR,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //find all the elements
        connectionStatusIcon = this.findViewById(R.id.connection_status_icon);

        if(!preliminaryTaskStatus) {
            //get Bluetooth permissions if not have
            doPreliminaryTasks();
        }
        testConnectionButton = (Button) findViewById(R.id.test_connection);
        testConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                establishConnection();
            }
        });
    }

    private void establishConnection(){
        if (mBluetoothService.establishConnection()){
            testConnectionButton.setVisibility(View.GONE);
            Toast.makeText(this, "Connection Successful with"+ bluetoothName +"!", Toast.LENGTH_SHORT).show();
            checkStatus();
        }
    }

    private boolean turnBluetoothOn() {
        if (mBluetoothAdapter == null) {
            currentStatus = BlueToothStatus.ERROR;
            Toast.makeText(this, "This device doesn't support bluetooth!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    private boolean checkBluetoothPairing(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (deviceName.equals(bluetoothName)){
                        mBluetoothDevice = device;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean connectBluetooth() {
        if(!checkBluetoothPairing()){
            Toast.makeText(this, "Please connect to " + this.bluetoothName + "!", Toast.LENGTH_SHORT).show();
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.android.settings",
                    "com.android.settings.bluetooth.BluetoothSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity( intent);
            return false;
        }
        return true;
    }

    private boolean testConnection() {
        //mBluetoothService = new BluetoothService(this);
        mBluetoothService = new BluetoothService(mBluetoothDevice, mBluetoothAdapter, this);
        Log.d(TAG, mBluetoothService.toString());
        return false;
    }


    private boolean checkStatus(){
        //if connecting
        connectionStatusIcon.setImageResource(R.color.colorConnecting);
        //if connected
        connectionStatusIcon.setImageResource(R.color.colorOkay);
        //if error
        connectionStatusIcon.setImageResource(R.color.colorError);

        return true;
        //if not
 //       return false;
    }


    private boolean getBluetoothPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                connectBluetooth();
            }
        }
    }

}
