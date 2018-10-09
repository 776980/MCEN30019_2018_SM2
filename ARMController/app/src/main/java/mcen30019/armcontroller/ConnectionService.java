package mcen30019.armcontroller;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

public interface ConnectionService {

    public boolean establishConnection();
    public boolean establishConnection(BluetoothDevice device);
    public boolean sendMessage(String msg);
}
