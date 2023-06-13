package com.example.final_project;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnector {
    private static final String TAG = "BluetoothConnector";
    public static final String ACTION_CONNECTION_SUCCESS = "com.example.final_project.CONNECTION_SUCCESS";
    public static final String ACTION_CONNECTION_FAILURE = "com.example.final_project.CONNECTION_FAILURE";
    public static final String EXTRA_DEVICE_NAME = "device_name";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver broadcastReceiver;
    private String targetDeviceName;

    public BluetoothConnector(Context context, String deviceName) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        targetDeviceName = deviceName;
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    String deviceName = device.getName();
                    if (deviceName != null && deviceName.equals(targetDeviceName)) {
                        // 디바이스를 찾았으므로 연결을 시도합니다.
                        connectToDevice(context, device);
                    }
                }
            }
        };
        // 블루투스 디바이스 검색을 위한 브로드캐스트 리시버를 등록합니다.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(broadcastReceiver, filter);
    }

    public void startBluetoothConnection(Context context) {
        // 블루투스가 활성화되어 있는지 확인합니다.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            // 블루투스가 비활성화 상태이므로 활성화 요청을 합니다.
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBluetoothIntent);
        } else {
            // 블루투스가 이미 활성화되어 있으므로 디바이스 검색을 시작합니다.
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothAdapter.startDiscovery();
        }
    }

    private void connectToDevice(Context context, BluetoothDevice device) {
        // 디바이스에 대한 연결 작업을 수행합니다.
        // 여기에 연결 코드를 작성하세요.
        // 예시로 BluetoothSocket을 생성하고 연결을 시도합니다.
        try {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            socket.connect();


            // 연결에 성공한 경우 처리합니다.
            Log.d(TAG, "Bluetooth connection success: " + device.getName());
            // 연결 성공을 알리는 브로드캐스트를 전송합니다.
            Intent successIntent = new Intent(ACTION_CONNECTION_SUCCESS);
            successIntent.putExtra(EXTRA_DEVICE_NAME, device.getName());
            context.sendBroadcast(successIntent);
        } catch (IOException e) {
            e.printStackTrace();
            // 연결 실패, 오류 처리를 수행합니다.
            Log.e(TAG, "Bluetooth connection failure: " + device.getName());
            // 연결 실패를 알리는 브로드캐스트를 전송합니다.
            Intent failureIntent = new Intent(ACTION_CONNECTION_FAILURE);
            failureIntent.putExtra(EXTRA_DEVICE_NAME, device.getName());
            context.sendBroadcast(failureIntent);
        }
    }

    public void stopBluetoothConnection(Context context) {
        // 블루투스 디바이스 검색을 위한 브로드캐스트 리시버를 해제합니다.
        context.unregisterReceiver(broadcastReceiver);
    }
}
