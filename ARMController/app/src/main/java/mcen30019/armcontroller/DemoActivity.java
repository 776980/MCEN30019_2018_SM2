package mcen30019.armcontroller;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

public class DemoActivity extends AppCompatActivity {
    Context context;
    SeekBar indexMiddle;
    SeekBar ringPinky;
    SeekBar thumbFinger;
    ArmController armController;

    private class SliderValue{
        SeekBar seekBar;
        Integer newValue;
        public SliderValue(SeekBar seekBar, Integer integer){
            this.newValue = integer;
            this.seekBar = seekBar;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        getAllSliders();
    }
    public void makeToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void getAllSliders() {
        indexMiddle = (SeekBar) findViewById(R.id.bar_index_middle);
        indexMiddle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                makeToast(progress + "");
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
                makeToast(progress + "");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        thumbFinger = (SeekBar) findViewById(R.id.bar_thumb);
        thumbFinger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                makeToast(progress + "");
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

}
