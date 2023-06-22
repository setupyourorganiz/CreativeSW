package com.example.CreativeSW;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.CreativeSW.dto.ArduinoDto;
import com.example.CreativeSW.dto.PathArduinoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
public class MainActivity extends AppCompatActivity {

    public static final String serverUrl = "http://3.35.191.211:8080/phone";
    double curr_x = 0;
    double curr_y = 0;
    ArrayList<ArduinoDto> whole_arudino = new ArrayList<>();
    ArrayList<PathArduinoDto> path_arduino = new ArrayList<>();
    ArrayList<String> wholeArduino_spin = new ArrayList<>();
    ArrayList<String> pathArduino_spin = new ArrayList<>();
    String userId = "by";
    final int max_bt_cnt = 10;

    WebView webView;
    Button btn_findRoute;
    Button btn_nowLocation;
    Button btn_user;
    Button btn_admin;
    Button btn_allArduino;
    Button btn_addArduino;
    EditText edit_startLocation;
    EditText edit_endLocation;
    EditText edit_arduinoName;
    EditText edit_arduinoLat;
    EditText edit_arduinoLng;
    LinearLayout linLayout_user;
    LinearLayout linLayout_manager;
    Spinner spin_wholeArduino;
    Spinner spin_pathArduino;
    FloatingActionButton floatBtn_color;

    int user_mode;
    String userColor = "red";
    int available_deleteDrawline;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private final Handler handler = new Handler();
    private int order = 0;
    private static String TARGET_DEVICE_NAME = "myBT05";
    private static String TARGET_DEVICE_ADDRESS = "98:D3:61:F6:57:C7";
    private BluetoothConnector bluetoothConnector;
    private static final BluetoothSocket TODO = null;
    final String TAG = MainActivity.class.getSimpleName();
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayList<String> btNameArray;
    ArrayList<String> deviceAddressArray;
    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothSocket btSocket = null;
    com.example.CreativeSW.ConnectedThread connectedThread;
    int curr_bt_cnt = 0;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        btn_findRoute = findViewById(R.id.find_route);   // 길찾기 버튼
        btn_nowLocation = findViewById(R.id.now_location);   //  현위치 버튼
        btn_admin = findViewById(R.id.btn_admin);
        btn_user = findViewById(R.id.btn_user);
        btn_allArduino = findViewById(R.id.btn_allArduino);
        btn_addArduino = findViewById(R.id.btn_addArduino);
        edit_startLocation = findViewById(R.id.start_location);
        edit_endLocation = findViewById(R.id.end_location);
        edit_arduinoName = findViewById(R.id.edit_arduinoName);
        edit_arduinoLat = findViewById(R.id.edit_arduinoLat);
        edit_arduinoLng = findViewById(R.id.edit_arduinoLng);
        linLayout_user = findViewById(R.id.linLayout_user);
        linLayout_manager = findViewById(R.id.linLayout_manager);
        spin_wholeArduino = findViewById(R.id.spin_wholeArduino);
        spin_pathArduino = findViewById(R.id.spin_pathArduino);
        floatBtn_color = findViewById(R.id.floatBtn_color);

        webView.addJavascriptInterface(new MainActivity.Bridge(this), "Bridge");
        btn_admin.setOnClickListener((v -> btnClicked_admin(v)));
        btn_findRoute.setOnClickListener((v -> btnClicked_findRoute(v)));
        btn_nowLocation.setOnClickListener((v->btnClicked_nowLocation(v)));
        btn_user.setOnClickListener((v -> btnClicked_user(v)));
        btn_allArduino.setOnClickListener((v -> btnClicked_allArduino(v)));
        btn_addArduino.setOnClickListener((v -> btnClicked_addArduino(v)));
        spin_wholeArduino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {spinItemChanged_allArduino(adapterView, view, i, l);}
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        spin_pathArduino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {spinItemChanged_pathArduino(adapterView, view, i, l);}
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        floatBtn_color.setOnClickListener((v -> floatBtnClicked_color(v)));

        user_mode = 1;
        available_deleteDrawline = 0;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        init_listener_location();
        init_webview();

        linLayout_manager.setVisibility(View.GONE);

        get_permission_location();
        getLocation();

        ArrayAdapter<String> adapter_wholeArduino = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wholeArduino_spin);
        adapter_wholeArduino.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spin_wholeArduino.setAdapter(adapter_wholeArduino);
        ArrayAdapter<String> adapter_pathArduino = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pathArduino_spin);
        adapter_pathArduino.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spin_pathArduino.setAdapter(adapter_pathArduino);


        deviceAddressArray = new ArrayList<>();
        btNameArray = new ArrayList<>();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null){
            Log.e("inapp","onCreate: device doesn't support bluetooth, null");
        }
        if (!btAdapter.isEnabled()) {
            Log.d("inapp","onCreate: device doesn't support bluetooth, unable");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
//        bluetoothConnector = new BluetoothConnector(this, TARGET_DEVICE_NAME);
//        bluetoothConnector.startBluetoothConnection(this);
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(BluetoothConnector.ACTION_CONNECTION_SUCCESS);
//        registerReceiver(connectionReceiver, filter);
    }

    @Override
    protected void onResume(){
        super.onResume();
        //bluetoothConnector.startBluetoothConnection(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(foundReceiver);
        //bluetoothConnector.stopBluetoothConnection(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(available_deleteDrawline == 1) {
                webView.loadUrl("javascript:toWeb_backBtn()");
                available_deleteDrawline = 0;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // 블루투스 관련 함수들
    public void search_bt() {
        Log.d("inapp", "funcName: search_bt: start");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d("inapp", "search_bt: no bluetooth permission");
            return;
        }
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        } else {
            if (btAdapter.isEnabled()) {
                btAdapter.startDiscovery();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(foundReceiver, filter);
            } else {
                Toast.makeText(getApplicationContext(), "bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
        Log.d("inapp", "funcName: search_bt: end");
    }
    public void pair_bt() {
        Log.d("inapp", "funcName: pair_bt: start");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btNameArray.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
            }
        }
        Log.d("inapp", "funcName: pair_bt: end");
    }


    void connect_bt(String name){
        Log.d("inapp", "funcName: connect_bt: start");
        curr_bt_cnt++;
        if(curr_bt_cnt > max_bt_cnt){
            return;
        }
        int idx = find_bt_idx(name);
        if(idx == -1){
            Log.d("inapp", "not exist such name of bluetooth device");
            return;
        }
        final String address = deviceAddressArray.get(idx); // get address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        BluetoothSocket btSocket;

        // create & connect socket
        try {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //btSocket = createBluetoothSocket(device);
            btSocket = device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
            btSocket.connect();

        } catch (IOException e) {
            Log.e("inapp", "connect_bt: " + e.getMessage());
            e.printStackTrace();
            connect_bt(name);
            return;
        }
        // start bluetooth communication
        connectedThread = new com.example.CreativeSW.ConnectedThread(btSocket);
        connectedThread.start();
        sendData_bt("1");
        Log.d("inapp", "funcName: connect_bt: end");
    }

    public void sendData_bt(String onOff) {
        if(onOff == "1"){
            runOnUiThread(() -> floatBtn_color.setClickable(false));
        }else{
            runOnUiThread(() -> floatBtn_color.setClickable(true));
        }
        Log.d("inapp", "funcName: sendData_bt, start");
        if (connectedThread != null) {
            PathArduinoDto currArduino = path_arduino.get(0);
            //String msg = Double.toString(currArduino.direction) + "," + userColor + "," + userId + "," + onOff;
            String msg = 270 + "," + userColor + "," + userId + "," + onOff;
            connectedThread.write(msg);
            Log.d("inapp", "sendData_bt: " + msg);
        }
        Log.d("inapp", "funcName: sendData_bt, end");
    }

    int find_bt_idx(String bt_name){
        if(btNameArray == null){
            Log.e("inapp", "find_bt_idx: btNameArray is null!");
            return -1;
        }
        int i = 0;
        for(i = 0; i< btNameArray.size(); i++) {
            String bt_device = btNameArray.get(i);
            if(bt_device == null){
                continue;
            }
            if(bt_device.equals(bt_name)){
                break;
            }
        }
        if(i == btNameArray.size()){
            return -1;
        }
        return i;
    }

    void init_bt_data(){
        btNameArray.clear();
        if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
            deviceAddressArray.clear();
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return TODO;
        }
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver foundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d("inapp", "funcName: onReceive: start");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btNameArray.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);

                pair_bt();
                connect_bt(TARGET_DEVICE_NAME);
            }
            Log.d("inapp", "funcName: onReceive: end");
        }
    };

//    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothConnector.ACTION_CONNECTION_SUCCESS.equals(action)) {
//                // 블루투스 연결 성공 처리
//                String deviceName = intent.getStringExtra(BluetoothConnector.EXTRA_DEVICE_NAME);
//                Log.d("MainActivity", "Bluetooth connection success: " + deviceName);
//
//            } else if (BluetoothConnector.ACTION_CONNECTION_FAILURE.equals(action)) {
//
//                // 블루투스 연결 실패 처리
//                String deviceName = intent.getStringExtra(BluetoothConnector.EXTRA_DEVICE_NAME);
//                Log.e("MainActivity", "Bluetooth connection failure: " + deviceName);
//            }
//        }
//    };



    void get_permission_location(){
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, REQUEST_LOCATION_PERMISSION);
    }
    boolean is_permitted_location(){
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }
    void init_listener_location() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocationInfo(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1,locationListener);
    }

    private void updateLocationInfo(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        //Toast.makeText(getApplicationContext(), ""+ distanceForCoord(curr_x,curr_y,longitude,latitude), Toast.LENGTH_LONG).show();
        this.curr_x = longitude;
        this.curr_y = latitude;
        if(isPassed_arduino()){
            //sendData_bt("0");
            if(path_arduino.size() > 0){
                //path_arduino.remove(0);
            }
        }
    }

    boolean isPassed_arduino(){
        if(path_arduino == null){
            return false;
        }
        if(path_arduino.size() < 1){
            return false;
        }
        double curr_arduino_x = path_arduino.get(0).arduino.lng;
        double curr_arduino_y = path_arduino.get(0).arduino.lat;
        if(path_arduino.size() == 1 && distanceForCoord(curr_x,curr_y,curr_arduino_x,curr_arduino_y) > 30){
            return true;
        }
        double next_arduino_x = path_arduino.get(1).arduino.lng;
        double next_arduino_y = path_arduino.get(1).arduino.lat;
        if(distanceForCoord(curr_x, curr_y, next_arduino_x, next_arduino_y) < distanceForCoord(curr_arduino_x, curr_arduino_y, next_arduino_x, next_arduino_y)){
            return true;
        }
        return false;
    }
    double distanceForCoord(double x1, double y1, double x2, double y2){
        double dx = 40075 * 1000 * (x2-x1) / 360;
        double dy = 40075 * 1000 * (y2-y1) / 360;
        return Math.sqrt(Math.pow(dx,2) + Math.pow(dy, 2));
    }

    void init_webview(){
        // 웹뷰 관련 내용들
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.loadUrl(serverUrl);
    }

    void init_edit_addArduinoInfo(){
        edit_arduinoName.setText("", TextView.BufferType.EDITABLE);
        edit_arduinoLat.setText("", TextView.BufferType.EDITABLE);
        edit_arduinoLng.setText("", TextView.BufferType.EDITABLE);
    }
    void init_edit_endpoint(){
        Editable start = edit_startLocation.getEditableText();
        Editable end = edit_endLocation.getEditableText();
        start.clear();
        end.clear();
    }

    void floatBtnClicked_color(View v){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(userColor == "red") {
                    floatBtn_color.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0, 255, 0)));
                    userColor = "green";
                }else if(userColor == "green"){
                    floatBtn_color.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0, 0, 255)));
                    userColor = "blue";
                }else{
                    floatBtn_color.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0, 0)));
                    userColor = "red";
                }
            }
        });
    }
    void btnClicked_findRoute(View v){
        String startLoc = edit_startLocation.getText().toString();
        String endLoc = edit_endLocation.getText().toString();
        if(!startLoc.equals("") && !endLoc.equals("")) {
            String start[] = startLoc.split(",");
            String end[] = endLoc.split(",");
            String x1 = start[0];
            String y1 = start[1];
            String x2 = end[0];
            String y2 = end[1];
            webView.loadUrl("javascript:toWeb_navigate(" + x1 + "," + y1 + "," + x2 + "," + y2 + ")");
            available_deleteDrawline = 1;

        }else {
            Log.d("inapp", "btnClicked_findRoute: arduino test");
            ArduinoDto arduino = new ArduinoDto(TARGET_DEVICE_NAME, 0,0);
            double dir = 0;
            PathArduinoDto test = new PathArduinoDto(arduino, dir);
            path_arduino.add(test);
            btNameArray.add(TARGET_DEVICE_NAME);
            deviceAddressArray.add(TARGET_DEVICE_ADDRESS);
            pair_bt();
            connect_bt(TARGET_DEVICE_NAME);
            //search_bt();

            Handler handler = new Handler();
            handler.postDelayed(() -> sendData_bt("0"), 10000);
        }
        init_edit_endpoint();
    }
    void btnClicked_nowLocation(View v){

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            get_permission_location();
        } else {
            getLocation();
            if(curr_x != 0 && curr_y != 0){
                String x = "127.0128954";
                String y = "37.5596518";
                x = "" + curr_x;
                y = "" + curr_y;
                webView.loadUrl("javascript:toWeb_currXy(" + x + "," + y + ")");
            }
        }
    }

    void btnClicked_admin(View v){
        linLayout_user.setVisibility(View.GONE);
        linLayout_manager.setVisibility(View.VISIBLE);

        user_mode = 0;
        available_deleteDrawline = 0;
    }

    void btnClicked_user(View v){
        linLayout_user.setVisibility(View.VISIBLE);
        linLayout_manager.setVisibility(View.GONE);

        user_mode = 1;
        available_deleteDrawline = 1;
    }
    void btnClicked_allArduino(View v){
        Log.d("inapp", "funcName: btnClicked_allArduino: start");

        String funcToinvoke = "javascript:toWeb_allArduino()";
        webView.loadUrl(funcToinvoke);

        Log.d("inapp", "funcName: btnClicked_allArduino: end");
    }
    void btnClicked_addArduino(View v){
        Log.d("inapp", "funcName: btnClicked_addArduino: start");

        String name = edit_arduinoName.getText().toString();
        String lat = edit_arduinoLat.getText().toString();
        String lng = edit_arduinoLng.getText().toString();
        init_edit_addArduinoInfo();
        if(name.equals("") || lat.equals("") || lng.equals("")){
            return;
        }
        String funcToinvoke = "javascript:toWeb_addArduino(\"" + name + "\"," + lng + "," + lat + ")";
        webView.loadUrl(funcToinvoke);

        Log.d("inapp", "funcName: btnClicked_addArduino: end");
    }

    void spinItemChanged_allArduino(AdapterView<?> parent, View view, int position, long id){
        Log.d("inapp", "funcName: spinItemChanged_allArduino: start");

        String item = (String)parent.getItemAtPosition(position);
        String tmp[] = item.split(": ");
        tmp = tmp[1].split(",");
        String lng = tmp[0];
        String lat = tmp[1];
        String funcToinvoke = "javascript:toWeb_listChange(" + lng + "," + lat + ")";
        webView.loadUrl(funcToinvoke);

        Log.d("inapp", "funcName: spinItemChanged_allArduino: end");
    }
    void spinItemChanged_pathArduino(AdapterView<?> parent, View view, int position, long id){
        spinItemChanged_allArduino(parent, view, position, id);
    }

    ArrayList<ArduinoDto> arr2arrList_arduinoDto(ArduinoDto[] arduinoDtos){
        ArrayList<ArduinoDto> res = new ArrayList<>();
        res.clear();
        for(int i=0; i< arduinoDtos.length; i++){
            res.add(arduinoDtos[i]);
        }
        return res;
    }
    String arduinoDto2Str(ArduinoDto arduinoDto){
        return arduinoDto.name + ": " + arduinoDto.lng + "," + arduinoDto.lat;
    }
    ArrayList<String> arduinoDtoList2StrList(ArrayList<ArduinoDto> arduinoDtos){
        ArrayList<String> res = new ArrayList<>();
        for(int i=0; i<arduinoDtos.size(); i++){
            String element = arduinoDto2Str(arduinoDtos.get(i));
            res.add(element);
        }
        return res;
    }

    ArrayList<PathArduinoDto> arr2arrList_pathArduinoDto(PathArduinoDto[] pathArduinoDtos){
        ArrayList<PathArduinoDto> res = new ArrayList<>();
        res.clear();
        for(int i=0; i< pathArduinoDtos.length; i++){
            res.add(pathArduinoDtos[i]);
        }
        return res;
    }
    String pathArduinoDto2Str(PathArduinoDto pathArduinoDto){
        return pathArduinoDto.arduino.name + ": " + pathArduinoDto.arduino.lng + "," + pathArduinoDto.arduino.lat;
    }
    ArrayList<String> pathArduinoDtoList2StrList(ArrayList<PathArduinoDto> pathArduinoDtos){
        ArrayList<String> res = new ArrayList<>();
        for(int i=0; i<pathArduinoDtos.size(); i++){
            String element = pathArduinoDto2Str(pathArduinoDtos.get(i));
            res.add(element);
        }
        return res;
    }

    void update_spin_wholeArduino(ArrayList<String> elements){
        wholeArduino_spin.clear();
        wholeArduino_spin.addAll(elements);
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spin_wholeArduino.getAdapter();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
    void update_spin_pathArduino(ArrayList<String> elements){
        pathArduino_spin.clear();
        pathArduino_spin.addAll(elements);
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spin_pathArduino.getAdapter();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    void update_edit_text(EditText edit, String text){
        runOnUiThread(() -> edit.setText(text, TextView.BufferType.EDITABLE));
    }

    public class Bridge{
        Context mContext;
        public Bridge(Context context){
            mContext = context;
        }

        @JavascriptInterface
        public void fromWeb_allArduino(final String arduinoList){
            if(arduinoList == null){
                Log.e("Javascript-inapp", "funcName: fromWeb_allArduino: Argument is null");
                return;
            }
            Log.d("Javascript-inapp", "funcName: fromWeb_allArduino:\n" + arduinoList);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ArduinoDto[] arduinoDtoList = objectMapper.readValue(arduinoList, ArduinoDto[].class);

                whole_arudino.clear();
                whole_arudino.addAll(arr2arrList_arduinoDto(arduinoDtoList));

            } catch (Exception e) {
                Log.e("Javascript-inapp", e.getMessage());
                e.printStackTrace();
            }
            update_spin_wholeArduino(arduinoDtoList2StrList(whole_arudino));
        }

        @JavascriptInterface
        public void fromWeb_pathArduino(final String pathArduinoList){
            if(pathArduinoList == null){
                Log.e("Javascript-inapp", "funcName: fromWeb_pathArduino: Argument is null");
                return;
            }
            Log.d("Javascript-inapp", "fromWeb_pathArduino:\n" + pathArduinoList);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                PathArduinoDto[] pathArduinoDtoList = objectMapper.readValue(pathArduinoList, PathArduinoDto[].class);

                // <실행할 함수 추가>
                path_arduino.clear();
                path_arduino.addAll(arr2arrList_pathArduinoDto(pathArduinoDtoList));

            } catch (Exception e) {
                Log.e("Javascript-inapp", e.getMessage());
                e.printStackTrace();
            }
            update_spin_pathArduino(pathArduinoDtoList2StrList(path_arduino));
            //search_bt();
        }

        // 마커 클릭 시 마커의 좌표를 가져옴
        @JavascriptInterface
        public void fromWeb_xy(double x, double y){
            String logMsg = "funcName: fromWeb_xy: " + x + ", " + y;
            Log.d("Javascript-inapp", logMsg);

            // <실행할 함수 추가>
        }

        @JavascriptInterface
        public void fromWeb_debug(final String message){
            Log.d("Javascript", message);

        }

        @JavascriptInterface
        public void fromWeb_err(final String message){
            Log.e("Javascript", message);

        }

        @JavascriptInterface
        public void fromWeb_markerClick(double x, double y){
            String logMsg = "fromWeb_markerClick: " + x + ", " + y;
            Log.d("Javascript-inapp", logMsg);

            if(user_mode == 1) {
                String xy = x + "," + y;
                if (edit_startLocation.getText().toString().equals("")) {
                    update_edit_text(edit_startLocation, xy);
                } else {
                    update_edit_text(edit_endLocation, xy);
                }
            }else if(user_mode == 0){
                update_edit_text(edit_arduinoLng, Double.toString(x));
                update_edit_text(edit_arduinoLat, Double.toString(y));
            }

            Log.d("Javascript-inapp", "funcName: fromWeb_markerClick, end");
        }

        @JavascriptInterface
        public void fromWeb_markerClick_currXy(double x, double y){
            String logMsg = "fromWeb_markerClick_currXy: " + x + ", " + y;
            Log.d("Javascript-inapp", logMsg);

            String xy = x + "," + y;
            update_edit_text(edit_startLocation, xy);
        }
    }
}