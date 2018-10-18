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
import android.widget.*;

import java.util.Timer;
import java.util.Set;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity"; //For logging into logcat
    private boolean preliminaryTaskStatus = false; //this flag is the check if bluetooth is on,
    // paired and all permissions are given

    private final int blinkRate = 300;
    private final String bluetoothName = "HC-06"; //Bluetooth to connect to
    private BlueToothStatus currentStatus = BlueToothStatus.UNCONNECTED; //When the app starts the bluetooth is unconne
    private ImageView connectionStatusIcon; //This imageview will display connection status
    public static BluetoothService mBluetoothService; //Bluetooth service personal class
    private BluetoothDevice mBluetoothDevice;  //device to connect to // same as BluetoothName

    private Button startConnectionButton;   //Start to connect //Hide when unconnected

    private Button callibrateButton;

    private Button ledOn;
    private Button ledOff;

    private Switch emgSwitch;

    private Blinker blinker;

    private int REQUEST_ENABLE_BT = 1;

    private void doPreliminaryTasks() {
        //check what task was being done after coming to the app
        if (getBluetoothPermissions()) {
            if (turnBluetoothOn()) {
                if (connectBluetooth()) {
                    if (testConnection()) {
                        //Everything okay
                        preliminaryTaskStatus = true;
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!preliminaryTaskStatus) {
            doPreliminaryTasks();
        } else {
            checkStatus();
        }
    }

    private enum BlueToothStatus {
        UNCONNECTED,
        CONNECTED,
        ERROR,
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //find all the elements
        getAllUIElements();
        if (!preliminaryTaskStatus) {
            //get Bluetooth permissions if not have
            doPreliminaryTasks();
        }
        if (preliminaryTaskStatus) {
            checkStatus();
        }
    }

    private void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void getAllUIElements() {
        connectionStatusIcon = this.findViewById(R.id.connection_status_icon);
        blinker = new Blinker(connectionStatusIcon, blinkRate);

        startConnectionButton = findViewById(R.id.start_connection_button);
        startConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startConnectionButton.getText().toString().equals(getResources().getString(R.string.connect))) {
                    try {
                        establishConnection();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    disconnectBluetooth();
                }
            }
        });

        ledOn = findViewById(R.id.led_on_button);
        ledOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("O");
            }
        });
        ledOff = findViewById(R.id.led_off_button);
        ledOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("F");
            }
        });
        callibrateButton = findViewById(R.id.callibrate_button);
        callibrateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openController();
            }
        });
        emgSwitch = this.findViewById(R.id.emg_switch);
        emgSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (currentStatus == BlueToothStatus.UNCONNECTED){
                    makeToast("Please connect first");
                    emgSwitch.setChecked(false);
                    isChecked = false;
                }
                if (!isChecked){
                    callibrateButton.setText("CONTROLLER");
                    callibrateButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            openController();
                        }
                    });
                    sendMessageToBluetooth("E2");
                }
                else {
                    callibrateButton.setText("CALLIBRATE");
                    callibrateButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            openCallibration();
                        }
                    });
                    sendMessageToBluetooth("E1");
                }
            }
        });
    }

    private void openController() {
        if (currentStatus == BlueToothStatus.UNCONNECTED){
            makeToast("Please connect first");
            return;
        }
        Intent controllerIntent = new Intent(this, ControllerActivity.class);
        startActivity(controllerIntent);
    }

    private void openCallibration() {
        Intent controllerIntent = new Intent(this, CallibrationActivity.class);
        startActivity(controllerIntent);
    }


    private void disconnectBluetooth() {
        mBluetoothService.disconnect();
        startConnectionButton.setText(R.string.connect);
        makeToast("Bluetooth disconnected");
        emgSwitch.setChecked(false);
        checkStatus();
    }



    private void establishConnection() throws InterruptedException {
        if (mBluetoothService.establishConnection() != null) {
            startConnectionButton.setText(R.string.disconnect);
            Toast.makeText(this, "Connection Successful with " + bluetoothName + "!", Toast.LENGTH_SHORT).show();
        } else {
            startConnectionButton.setText(R.string.connect);
            Toast.makeText(this, "Connection to " + bluetoothName + "failed", Toast.LENGTH_SHORT).show();
        }
        checkStatus();
    }

    private boolean turnBluetoothOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //mobile phones adapter
        // only need once
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

    private boolean checkBluetoothPairing() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //mobile phones adapter
        // only need once
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                if (deviceName.equals(bluetoothName)) {
                    mBluetoothDevice = device;
                    return true;
                }
            }
        }
        return false;
    }


    private boolean connectBluetooth() {
        if (!checkBluetoothPairing()) {
            Toast.makeText(this, "Please connect to " + this.bluetoothName + "!", Toast.LENGTH_SHORT).show();
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.android.settings",
                    "com.android.settings.bluetooth.BluetoothSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return false;
        }
        return true;
    }

    private boolean testConnection() {
        //mBluetoothService = new BluetoothService(this);
        if (mBluetoothDevice == null) {
            return false;
        }
        mBluetoothService = new BluetoothService(mBluetoothDevice, this);
        Log.d(TAG, mBluetoothService.toString());
        return true;
    }


    private void checkStatus() {
        //Everything is unconnected
        if (!mBluetoothService.checkSocketConnection()) {
            currentStatus = BlueToothStatus.UNCONNECTED;
        } else {
            currentStatus = BlueToothStatus.CONNECTED;
        }
        blinker.changeBlinker(currentStatus);
    }


    private boolean getBluetoothPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
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

    public static boolean sendMessageToBluetooth(String msg) {
        mBluetoothService.sendMessage(msg);
        return false;
    }

    private class Blinker{
        ImageView icon;
        SchedulerThread schedulerThread;
        Blinker(ImageView imageIcon, int blinkRate){
            this.icon = imageIcon;
            schedulerThread = new SchedulerThread(imageIcon, blinkRate);
        }

        void changeBlinker(BlueToothStatus currentStatus){
            schedulerThread.notifyBlinkerThread(currentStatus);
        }

        private class SchedulerThread{
            private Timer timer = new Timer();
            private BlinkerThread blinkerThread;
            SchedulerThread(ImageView imageIcon, int blinkRate){
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                blinkerThread.go();
                            }
                        });
                    }
                }, blinkRate, blinkRate);
                blinkerThread = new BlinkerThread(imageIcon);
            }

            void notifyBlinkerThread(BlueToothStatus currentStatus) {
                blinkerThread.statusChanged(currentStatus);
            }
        }

        private class BlinkerThread{
            BlueToothStatus currentStatus = BlueToothStatus.UNCONNECTED;
            ImageView imageView;
            private boolean flag = false;
            BlinkerThread(ImageView imageView){
                this.imageView = imageView;
            }
            void go() {
                if (currentStatus == BlueToothStatus.UNCONNECTED){
                    if (flag) {
                        imageView.setImageResource(R.color.colorConnecting);
                    }
                    else {
                        imageView.setImageResource(R.color.colorError);
                    }
                }
                else if(currentStatus == BlueToothStatus.CONNECTED){
                    if (flag) {
                        imageView.setImageResource(R.color.colorOkay);
                    }
                    else {
                        imageView.setImageResource(R.color.colorConnecting);
                    }
                }
                flag = !flag;
            }
            void statusChanged(BlueToothStatus currentStatus) {
                this.currentStatus = currentStatus;
            }
        }
    }
}
