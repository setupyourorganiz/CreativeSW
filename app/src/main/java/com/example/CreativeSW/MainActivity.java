package com.example.CreativeSW;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
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
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.util.Log;

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

@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
public class MainActivity extends AppCompatActivity {
    private static final BluetoothSocket TODO = null;
    final String TAG = MainActivity.class.getSimpleName();
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // 블루투스 관련
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;
    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothSocket btSocket = null;
    com.example.CreativeSW.ConnectedThread connectedThread;

    TextView textStatus;
    Button btnPaired, btnSearch, btnSend1;
    ListView listView;
    WebView webView = null;
    Button btn_findRoute;
    Button btn_nowLocation;
    Button btn_user;
    EditText edit_startLocation;
    EditText edit_endLocation;
    Button btn_nextIntent;
    LinearLayout linLayout_user;
    LinearLayout linLayout_manager;
    Spinner spin_wholearduino;
    Spinner spin_inroutearduino;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private final Handler handler = new Handler();
    private int order = 0;
    ArrayList<String> strings = new ArrayList<>();  // 임시사용한 배열 변수
    public static final String serverUrl = "http://3.35.191.211:8080/phone";

    private static String TARGET_DEVICE_NAME = "";
    private BluetoothConnector bluetoothConnector;
    double curr_x = 0;
    double curr_y = 0;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btn_nextIntent = findViewById(R.id.next_intent);
        btn_user = findViewById(R.id.btn_user);
        webView = findViewById(R.id.webview);
        btn_findRoute = findViewById(R.id.find_route);   // 길찾기 버튼
        btn_nowLocation = findViewById(R.id.now_location);   //  현위치 버튼
        edit_startLocation = findViewById(R.id.start_location);
        edit_endLocation = findViewById(R.id.end_location);
        linLayout_user = findViewById(R.id.linLayout_user);
        linLayout_manager = findViewById(R.id.linLayout_manager);
        spin_wholearduino = findViewById(R.id.whole_arduino);
        spin_inroutearduino = findViewById(R.id.in_route_arduino);

        btn_nextIntent.setOnClickListener((v -> btnClicked_nextIntent(v)));
        webView.addJavascriptInterface(new MainActivity.Bridge(this), "Bridge");
        btn_findRoute.setOnClickListener((v -> btnClicked_findRoute(v)));
        btn_nowLocation.setOnClickListener((v->btnClicked_nowLocation(v)));
        btn_user.setOnClickListener((v -> btnClicked_user(v)));


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        init_listener_location();
        init_webview();

        linLayout_manager.setVisibility(View.GONE);

        // Get permission
        get_permission_location();
        getLocation();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,android.R.layout.simple_spinner_item, strings
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spin_wholearduino.setAdapter(adapter);

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
    void init_listener_location(){
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
        // 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정
        webSettings.setLoadWithOverviewMode(true);
        // javascript의 window.open 허용
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.loadUrl(serverUrl);
    }

    // GPS에 대한 함수들
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }
    void btnClicked_nextIntent(View v){
        linLayout_user.setVisibility(View.GONE);
        linLayout_manager.setVisibility(View.VISIBLE);
        //Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
        //startActivity(intent);
    }

    void btnClicked_user(View v){
        linLayout_user.setVisibility(View.VISIBLE);
        linLayout_manager.setVisibility(View.GONE);
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
        init_edit_endpoint();
        webView.loadUrl("javascript:toWeb_navigate(" + x1 + "," + y1 + "," + x2 + "," + y2 + ")");
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

    void update_spin_wholeArduino(ArduinoDto[] arduinoDtoList){
        spin_wholearduino.invalidate();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spin_wholearduino.getAdapter();
        strings.clear();
        for(int i=0; i<arduinoDtoList.length; i++){
            ArduinoDto arduinoDto = arduinoDtoList[i];
            String element = arduinoDto.name + ": " + arduinoDto.lng + "," + arduinoDto.lat;
            strings.add(element);
        }
        adapter.notifyDataSetChanged();
    }

    public class Bridge{
        Context mContext;
        public Bridge(Context context){
            mContext = context;
        }

        // 현재 등록된 모든 아두이노 정보를 가져옴
        @JavascriptInterface
        public void fromWeb_allArduino(final String arduinoList){
            if(arduinoList == null){
                Log.e("Javascript-inapp", "Bridge1 fromWeb_allArduino: Argument is null");
                return;
            }
            Log.d("Javascript-inapp", "Bridge1 fromWeb_allArduino:\n" + arduinoList);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ArduinoDto[] arduinoDtoList = objectMapper.readValue(arduinoList, ArduinoDto[].class);

                // TARGET_DEVICE_NAME에 들어있는 이름과 같은 기기를 발견하면 해당 기기와 연결
                update_spin_wholeArduino(arduinoDtoList);

            } catch (Exception e) {
                Log.e("Javascript", e.getMessage());
                e.printStackTrace();
            }
        }

        // 길찾기에서 경로 상에 있는 사거리 아두이노 리스트를 가져옴.
        @JavascriptInterface
        public void fromWeb_pathArduino(final String pathArduinoList){
            if(pathArduinoList == null){
                Log.e("Javascript", "fromWeb_pathArduino: Argument is null");
                return;
            }
            Log.d("Javascript", "fromWeb_pathArduino:\n" + pathArduinoList);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                PathArduinoDto[] pathArduinoDtoList = objectMapper.readValue(pathArduinoList, PathArduinoDto[].class);

                // <실행할 함수 추가>
                for(PathArduinoDto e : pathArduinoDtoList){
                    String msg = e.arduino.name + ": " + e.direction;
                    Log.d("Javascript", msg);
                }

            } catch (Exception e) {
                Log.e("Javascript", e.getMessage());
                e.printStackTrace();
            }
        }

        // 마커 클릭 시 마커의 좌표를 가져옴
        @JavascriptInterface
        public void fromWeb_xy(double x, double y){
            String logMsg = "fromWeb_xy: " + x + ", " + y;
            Log.d("Javascript", logMsg);

            // <실행할 함수 추가>
        }

        // 자바스크립트 디버그를 안드로이드에 표시
        @JavascriptInterface
        public void fromWeb_debug(final String message){ // 블루투스 이름과 방향을 전달받는다
            Log.d("Javascript", message);

        }

        // 자바스크립트 에러를 안드로이드에 표시
        @JavascriptInterface
        public void fromWeb_err(final String message){ // 블루투스 이름과 방향을 전달받는다
            Log.e("Javascript", message);

        }

        @JavascriptInterface
        public void fromWeb_markerClick(double x, double y){
            String logMsg = "fromWeb_markerClick: " + x + ", " + y;
            Log.d("Javascript", logMsg);

            String xy = x + "," + y;
            if(edit_startLocation.getText().toString().equals("")){
                Editable editable = edit_startLocation.getEditableText();
                editable.clear();
                editable.append(xy);
            }else{
                Editable editable = edit_endLocation.getEditableText();
                editable.clear();
                editable.append(xy);
            }
        }
    }
}