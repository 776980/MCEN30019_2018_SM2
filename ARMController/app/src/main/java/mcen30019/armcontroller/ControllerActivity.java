package mcen30019.armcontroller;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

public class ControllerActivity extends AppCompatActivity {
    Context context;
    SeekBar indexMiddle;
    SeekBar ringPinky;
    SeekBar thumbFinger;
    BluetoothService mBluetoothService;
    private class SliderValue{
        SeekBar seekBar;
        Integer newValue;
        public SliderValue(SeekBar seekBar, Integer integer){
            this.newValue = integer;
            this.seekBar = seekBar;
        }
    }

    Button expandAllButton;
    Button contractAllButton;
    Button grabBallButton;
    Button grabMarkerButton;
    Button grabHammerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        getAllSliders();
        getAllButtons();
        mBluetoothService = MainActivity.mBluetoothService;
        Toast.makeText(this, "Socket is " + mBluetoothService.checkSocketConnection(), Toast.LENGTH_SHORT);
    }

    private void getAllButtons() {
        contractAllButton = (Button) findViewById(R.id.button_contract);
        contractAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("A"+180);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToBluetooth("B"+180);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToBluetooth("C"+180);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        expandAllButton = (Button) findViewById(R.id.button_expand);
        expandAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("A"+0);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToBluetooth("B"+0);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToBluetooth("C"+0);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });grabMarkerButton = (Button) findViewById(R.id.button_marker);
        grabMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("A"+50);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToBluetooth("B"+50);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToBluetooth("C"+50);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });grabHammerButton = (Button) findViewById(R.id.button_hammer);
        grabHammerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("A"+50);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToBluetooth("B"+50);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToBluetooth("C"+50);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        grabBallButton = (Button) findViewById(R.id.button_ball);
        grabBallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("A"+50);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToBluetooth("B"+50);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToBluetooth("C"+50);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void makeToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void getAllSliders() {
        thumbFinger = (SeekBar) findViewById(R.id.bar_thumb);
        thumbFinger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0){
                    sendMessageToBluetooth("A"+ 0);
                }
                else {
                    sendMessageToBluetooth("A" + (progress - 1));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        ringPinky = (SeekBar) findViewById(R.id.bar_ring_pinky);
        ringPinky.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0){
                    sendMessageToBluetooth("B"+ 0);
                }
                else {
                    sendMessageToBluetooth("B" + (99-(progress - 1)));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        indexMiddle = (SeekBar) findViewById(R.id.bar_index_middle);
        indexMiddle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0){
                    sendMessageToBluetooth("C"+ 0);
                }
                else {
                    sendMessageToBluetooth("C" + (progress - 1));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public SliderValue changeInSlider(SeekBar seekBar, Integer integer){
        return new SliderValue(seekBar, integer);
    }

    public void sendMessageToBluetooth(String msg){
        mBluetoothService.sendMessage(msg);
    }
}
