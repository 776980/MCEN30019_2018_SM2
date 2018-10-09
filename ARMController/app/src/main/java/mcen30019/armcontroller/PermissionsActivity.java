package mcen30019.armcontroller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class PermissionsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {


    private final String[] PERMISSIONS_BLUETOOTH = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BODY_SENSORS };
    private int REQUEST_BLUETOOTH = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        requestBluetoothPermissions();

    }

    private boolean checkBluetoothPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                //This one is only for testing can remove
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothPermissions(){
        if (checkBluetoothPermissions()){
            finish();
            return;
        }
        ActivityCompat.requestPermissions(this, PERMISSIONS_BLUETOOTH, REQUEST_BLUETOOTH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH) {
        // We have requested multiple permissions for contacts, so all of them need to be
            if (verifyPermissionsResult(grantResults)) {
                // All required permissions have been granted, display contacts fragment.
                finish();
            } else {
                Toast.makeText(this, "Sorry the app requires, these permissions to run!", Toast.LENGTH_LONG).show();
                requestBluetoothPermissions();
            }
            // checked.
        }
    }

    private static boolean verifyPermissionsResult(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }
        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}

