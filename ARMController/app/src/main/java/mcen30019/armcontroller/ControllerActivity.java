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
    private class SliderValue{
        SeekBar seekBar;
        Integer newValue;
        public SliderValue(SeekBar seekBar, Integer integer){
            this.newValue = integer;
            this.seekBar = seekBar;
        }
    }

    private Button expandAllButton;
    private Button contractAllButton;
    private Button grabBallButton;
    private Button grabMarkerButton;
    private Button grabHammerButton;
    private Button wavingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        getAllSliders();
        getAllButtons();
    }

    private void getAllButtons() {
        contractAllButton = (Button) findViewById(R.id.button_contract);
        contractAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("C");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        expandAllButton = (Button) findViewById(R.id.button_expand);
        expandAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("X");
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
                sendMessageToBluetooth("M");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        grabHammerButton = (Button) findViewById(R.id.button_hammer);
        grabHammerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("H");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        grabBallButton = (Button) findViewById(R.id.button_ball);
        grabBallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("B");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        wavingButton = (Button) findViewById(R.id.wave_button);
        wavingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToBluetooth("W");
                try {
                    Thread.sleep(100);
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
                sendMessageToBluetooth("T" + progress);
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
                sendMessageToBluetooth("R" + progress);
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
                sendMessageToBluetooth("I" + progress);
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

    private void sendMessageToBluetooth(String msg){
        MainActivity.sendMessageToBluetooth(msg);
    }
}
