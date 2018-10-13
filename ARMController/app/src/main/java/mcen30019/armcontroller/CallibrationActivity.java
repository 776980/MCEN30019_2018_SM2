package mcen30019.armcontroller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CallibrationActivity extends AppCompatActivity {
    private TextView instructionsText;
    private Button buttonNext;
    private int pageNo = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callibration);
        instructionsText = findViewById(R.id.text_instruction);
        buttonNext = findViewById(R.id.button_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNo++;
                callibrationNext();
            }
        });
    }

    private void callibrationNext() {
        switch (pageNo){
            case 1:
                instructionsText.setText("Please try to contract hand twice in a sequence and press next.");
                break;
            case 2:
                instructionsText.setText("Please try to expand hand twice in a sequence and press next.");
                break;
            case 3:
                instructionsText.setText("Callibration successful now you can use your ARM.");
                buttonNext.setText("FINISH");
                break;
            case 4:
                finish();
        }
    }
}
