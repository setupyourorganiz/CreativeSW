package com.example.CreativeSW;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.Context;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
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
import android.widget.ListView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.util.Log;

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

@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
public class MainActivity extends AppCompatActivity {

    public static final String serverUrl = "http://3.35.191.211:8080/phone";
    double curr_x = 0;
    double curr_y = 0;
    ArrayList<ArduinoDto> whole_arudino = new ArrayList<>();
    ArrayList<PathArduinoDto> path_arduino = new ArrayList<>();
    ArrayList<String> wholeArduino_spin = new ArrayList<>();
    ArrayList<String> pathArduino_spin = new ArrayList<>();

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

    int user_mode;


    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private final Handler handler = new Handler();
    private int order = 0;
    private static String TARGET_DEVICE_NAME = "";
    private BluetoothConnector bluetoothConnector;
    private static final BluetoothSocket TODO = null;
    final String TAG = MainActivity.class.getSimpleName();
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;
    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothSocket btSocket = null;
    com.example.CreativeSW.ConnectedThread connectedThread;

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

        user_mode = 1;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        init_listener_location();
        init_webview();

        linLayout_manager.setVisibility(View.GONE);

        // Get permission
        get_permission_location();
        getLocation();

        ArrayAdapter<String> adapter_wholeArduino = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wholeArduino_spin);
        adapter_wholeArduino.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spin_wholeArduino.setAdapter(adapter_wholeArduino);
        ArrayAdapter<String> adapter_pathArduino = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pathArduino_spin);
        adapter_pathArduino.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spin_pathArduino.setAdapter(adapter_pathArduino);

        // 블루투스 관련 내용들


        // Enable bluetooth
//        bluetoothbtAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (!btAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }


        // variables
//        textStatus = (TextView) findViewById(R.id.text_status);
//        btnPaired = (Button) findViewById(R.id.btn_paired);
//        btnSearch = (Button) findViewById(R.id.btn_search);
//        btnSend1 = (Button) findViewById(R.id.btn_send1);
//        listView = (ListView) findViewById(R.id.listview);

        // Show paired devices
        //btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        //deviceAddressArray = new ArrayList<>();
        //listView.setAdapter(btArrayAdapter);

        //listView.setOnItemClickListener(new myOnItemClickListener());


        //bluetoothConnector = new BluetoothConnector(this, TARGET_DEVICE_NAME);
        //bluetoothConnector.startBluetoothConnection(this);

        //IntentFilter filter = new IntentFilter();
        //filter.addAction(BluetoothConnector.ACTION_CONNECTION_SUCCESS);
        //filter.addAction(BluetoothConnector.ACTION_CONNECTION_SUCCESS);
        //registerReceiver(connectionReceiver, filter);
    }
    // BluetoothConnector의 블루투스 연결을 시작하는 메소드 호출
    @Override
    protected void onResume(){
        super.onResume();

        // 블루투스 연결 시작
        //bluetoothConnector.startBluetoothConnection(getApplicationContext());
    }

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
        this.curr_x = longitude;
        this.curr_y = latitude;
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    // 블루투스 관련 함수들
//    public void onClickButtonPaired(View view) {
//        btArrayAdapter.clear();
//        if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
//            deviceAddressArray.clear();
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        pairedDevices = btAdapter.getBondedDevices();
//        if (pairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : pairedDevices) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                btArrayAdapter.add(deviceName);
//                deviceAddressArray.add(deviceHardwareAddress);
//            }
//        }
//    }

//    public void onClickButtonSearch(View view) {
//        // Check if the device is already discovering
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        if (btAdapter.isDiscovering()) {
//            btAdapter.cancelDiscovery();
//        } else {
//            if (btAdapter.isEnabled()) {
//                btAdapter.startDiscovery();
//                btArrayAdapter.clear();
//                if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
//                    deviceAddressArray.clear();
//                }
//                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//                registerReceiver(receiver, filter);
//            } else {
//                Toast.makeText(getApplicationContext(), "bluetooth not on", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    // Send string "a"
//    public void onClickButtonSend1(View view) {
//        if (connectedThread != null) {
//            connectedThread.write("a");
//        }
//    }

    // Create a BroadcastReceiver for ACTION_FOUND.
//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                // Discovery has found a device. Get the BluetoothDevice
//                // object and its info from the Intent.
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                btArrayAdapter.add(deviceName);
//                deviceAddressArray.add(deviceHardwareAddress);
//                btArrayAdapter.notifyDataSetChanged();
//            }
//        }
//    };
//
//    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothConnector.ACTION_CONNECTION_SUCCESS.equals(action)) {
//                String deviceName = intent.getStringExtra(BluetoothConnector.EXTRA_DEVICE_NAME);
//                // 블루투스 연결 성공 처리
//                Log.d("MainActivity", "Bluetooth connection success: " + deviceName);
//            } else if (BluetoothConnector.ACTION_CONNECTION_FAILURE.equals(action)) {
//                String deviceName = intent.getStringExtra(BluetoothConnector.EXTRA_DEVICE_NAME);
//                // 블루투스 연결 실패 처리
//                Log.e("MainActivity", "Bluetooth connection failure: " + deviceName);
//            }
//        }
//    };

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        // Don't forget to unregister the ACTION_FOUND receiver.
//        unregisterReceiver(receiver);
//
//        // 두번째 블루투스 연결 종료
//        bluetoothConnector.stopBluetoothConnection(this);
//    }

//    public class myOnItemClickListener implements AdapterView.OnItemClickListener {

//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            Toast.makeText(getApplicationContext(), btArrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();
//
//            textStatus.setText("try...");
//
//            final String name = btArrayAdapter.getItem(position); // get name
//            final String address = deviceAddressArray.get(position); // get address
//            boolean flag = true;
//
//            BluetoothDevice device = btAdapter.getRemoteDevice(address);
//
//            // create & connect socket
//            try {
//                btSocket = createBluetoothSocket(device);
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                btSocket.connect();
//            } catch (IOException e) {
//                flag = false;
//                textStatus.setText("connection failed!");
//                e.printStackTrace();
//            }
//
//            // start bluetooth communication
//            if (flag) {
//                textStatus.setText("connected to " + name);
//                connectedThread = new com.example.CreativeSW.ConnectedThread(btSocket);
//                connectedThread.start();
//            }
//
//        }
//    }

//    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
//        try {
//            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
//            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
//        } catch (Exception e) {
//            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return TODO;
//        }
//        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
//    }


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
    void btnClicked_findRoute(View v){
        String start[] = edit_startLocation.getText().toString().split(",");
        String end[] = edit_endLocation.getText().toString().split(",");
        String x1 = start[0];
        String y1 = start[1];
        String x2 = end[0];
        String y2 = end[1];
        webView.loadUrl("javascript:toWeb_navigate(" + x1 + "," + y1 + "," + x2 + "," + y2 + ")");
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
    }

    void btnClicked_user(View v){
        linLayout_user.setVisibility(View.VISIBLE);
        linLayout_manager.setVisibility(View.GONE);

        user_mode = 1;
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
        edit.setText(text, TextView.BufferType.EDITABLE);
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
            Log.d("Javascript-inapp", "funcName: fromWeb_pathArduino:\n" + pathArduinoList);
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