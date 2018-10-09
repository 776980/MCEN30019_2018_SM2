package mcen30019.armcontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private boolean preliminaryTaskStatus = false;
    private final String ledOnMessage = "O";
    private final String ledOffMessage = "F";
    private final String bluetoothName = "HC-06";

    private static BlueToothStatus currentStatus = BlueToothStatus.UNCONNECTED;
    private static ImageView connectionStatusIcon;
    private BluetoothService mBluetoothService;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private Button startConnectionButton;
    private Button ledOnButton;
    private Button ledButtonOff;
    private Button contractHandButton;
    private Button startDemoButton;
    private ArmController armController;

    private int REQUEST_ENABLE_BT = 1;

    private boolean doPreliminaryTasks() {
        //check what task was being done after coming to the app
        if (getBluetoothPermissions()) {
            if (turnBluetoothOn()) {
                if (connectBluetooth()) {
                    if (testConnection()) {
                        //Everything okay
                        preliminaryTaskStatus = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!preliminaryTaskStatus){
            doPreliminaryTasks();
        };
        //Do your work
    }

    private enum BlueToothStatus{
        UNCONNECTED,
        CONNECTED,
        ERROR,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //find all the elements
        connectionStatusIcon = this.findViewById(R.id.connection_status_icon);
        getAllUIButtons();
        if(!preliminaryTaskStatus) {
            //get Bluetooth permissions if not have
            doPreliminaryTasks();
        }
        try {
            if(preliminaryTaskStatus){
                checkStatus();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printSomething() {
        Toast.makeText(this, "Something being printed", Toast.LENGTH_SHORT);
    }

    private void getAllUIButtons(){
        startConnectionButton = (Button) findViewById(R.id.start_connection_button);
        startConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Trying to connect!");
                    printSomething();
                    establishConnection();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        ledOnButton = (Button) findViewById(R.id.led_on);
        ledOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ledOn();
            }
        });
        ledButtonOff = (Button) findViewById(R.id.led_off);
        ledButtonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ledOff();
            }
        });
        contractHandButton = (Button) findViewById(R.id.contract_hand_button);
        contractHandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                armController.contractHand();
            }
        });
        startDemoButton = (Button) findViewById(R.id.start_demo_button);
        startDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDemo();
            }
        });
    }


    private void ledOn() {
        sendMessageToBluetooth(ledOnMessage);
    }
    private void ledOff() {
        sendMessageToBluetooth(ledOffMessage);
    }

    private void startDemo() {
        //TODO
    }


    private void establishConnection() throws InterruptedException {
        Toast.makeText(this, "Trying to connect"+ bluetoothName +"!", Toast.LENGTH_SHORT).show();
        if (mBluetoothService.establishConnection() != null){
            startConnectionButton.setVisibility(View.GONE);
            Toast.makeText(this, "Connection Successful with"+ bluetoothName +"!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Connection to "+ bluetoothName +"failed", Toast.LENGTH_SHORT).show();
        }
        checkStatus();
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
            Toast.makeText(this, "Please turn bluetooth on!", Toast.LENGTH_SHORT).show();
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
        if (mBluetoothDevice == null){
            return false;
        }
        mBluetoothService = new BluetoothService(mBluetoothDevice, this);
        Log.d(TAG, mBluetoothService.toString());
        return true;
    }


    private boolean checkStatus() throws InterruptedException {
        //Everything is unconnected
        if (!mBluetoothService.checkSocketConnection()){
            currentStatus = BlueToothStatus.UNCONNECTED;
        }
        else {
            currentStatus = BlueToothStatus.CONNECTED;
        }

//        //if connecting
//        connectionStatusIcon.setImageResource(R.color.colorConnecting);
//        //if connected
//        connectionStatusIcon.setImageResource(R.color.colorOkay);
//        //if error
//        connectionStatusIcon.setImageResource(R.color.colorError);
        statusLight(currentStatus);
        return true;
        //if not
 //       return false;
    }

    private void statusLight(final BlueToothStatus currentStatus) throws InterruptedException {
        Thread thread;
        switch (currentStatus) {
            case UNCONNECTED:
                thread = new Thread(){
                @Override
                public void run(){
                        try {
                            while (true) {
                                if (MainActivity.currentStatus == BlueToothStatus.UNCONNECTED) {
                                    Thread.sleep(500);  //1000ms = 1 sec
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            connectionStatusIcon.setImageResource(R.color.colorError);
                                        }
                                    });
                                    Thread.sleep(500);  //1000ms = 1 sec
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            connectionStatusIcon.setImageResource(R.color.colorConnecting);
                                        }
                                    });
                                }
                                else {
                                    break;
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
                break;
            case CONNECTED:
                thread = new Thread(){
                @Override
                public void run(){
                        try {
                            while (true) {
                                if (MainActivity.currentStatus == BlueToothStatus.CONNECTED) {
                                    Thread.sleep(500);  //1000ms = 1 sec
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            connectionStatusIcon.setImageResource(R.color.colorConnecting);
                                        }
                                    });
                                    Thread.sleep(500);  //1000ms = 1 sec
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            connectionStatusIcon.setImageResource(R.color.colorOkay);
                                        }
                                    });
                                }
                                else {
                                    break;
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
                break;
            case ERROR:
                connectionStatusIcon.setImageResource(R.color.colorError);
                break;
            default:
                connectionStatusIcon.setImageResource(R.color.colorConnecting);
                break;
        }
    }


    private boolean getBluetoothPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
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

    private boolean sendMessageToBluetooth(String msg) {
        mBluetoothService.sendMessage(msg);
        return false;
    }

}
