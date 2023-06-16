package com.example.CreativeSW;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.CreativeSW.dto.ArduinoDto;
import com.example.CreativeSW.dto.PathArduinoDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public class WebViewActivity extends AppCompatActivity {
    private WebView webView = null;

    Button btn_regist;
    Button btn_callin;
    Spinner spin_wholeArduino;
    Spinner spin_pathArduino;
    EditText edit_arduinoName;
    EditText edit_arduinoLat;
    EditText edit_arduinoLng;
    String marker_x;
    String marker_y;
    ArrayList<String> strings = new ArrayList<>();  // 임시사용한 배열 변수
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subactivity_main);

        Button imageButton = findViewById(R.id.next_layout);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        webView = findViewById(R.id.webview1);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
            }
        });

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

        webView.loadUrl(MainActivity.serverUrl);

        webView.addJavascriptInterface(new WebViewActivity.Bridge(this), "Bridge");

        // 버튼들 연결
        btn_regist = findViewById(R.id.btn_addArduino);
        btn_callin = findViewById(R.id.btn_allArduino);

        spin_wholeArduino = findViewById(R.id.spin_wholeArduino);
        spin_pathArduino = findViewById(R.id.spin_pathArduino);

        edit_arduinoName = findViewById(R.id.edit_arduinoName);
        edit_arduinoLat = findViewById(R.id.edit_arduinoLat);
        edit_arduinoLng = findViewById(R.id.edit_arduinoLng);

        // 스피너(리스트박스) 사용을 위한 설정 및 함수들

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,android.R.layout.simple_spinner_item, strings
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spin_wholeArduino.setAdapter(adapter);
        //spin_inroutearduino.setAdapter(adapter);
        spin_wholeArduino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                // 선택되면 사용할 함수 작성
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                // 선택되지 않았을 때
            }
        });
        spin_pathArduino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                // 선택되면 사용할 함수 작성
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                // 선택되지 않았을 때
            }
        });

        // 아두이노 등록하기 버튼
        btn_regist.setOnClickListener((new View.OnClickListener(){
            public void onClick(View v){
                webView.loadUrl("javascript:toWeb_addArduino(name, x, y)");
            }
        }));

        // 아두이노 리스트 불러오기 버튼
        btn_callin.setOnClickListener((new View.OnClickListener(){
            public void onClick(View v){
                webView.loadUrl("javascript:toWeb_allArduino()");
            }
        }));
    }

    void btnClicked_regist(View v){
        webView.loadUrl("javascript:toWeb_addArduino(" + edit_arduinoName + "," + edit_arduinoLat + "," + edit_arduinoLng + ")");
    }

    void btnClicked_callin(View v){
        webView.loadUrl("javascript:toWeb_allArduino()");
    }

    void update_spin_wholeArduino(ArduinoDto[] arduinoDtoList){
        spin_wholeArduino.invalidate();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spin_wholeArduino.getAdapter();
        strings.clear();
        for(int i=0; i<arduinoDtoList.length; i++){
            ArduinoDto arduinoDto = arduinoDtoList[i];
            String element = arduinoDto.name + ": " + arduinoDto.lng + "," + arduinoDto.lat;
            strings.add(element);
        }
        adapter.notifyDataSetChanged();
    }

    // 웹과 주고받는 함수들
    public class Bridge{
        Context mContext;
        public Bridge(Context context){
            mContext = context;
        }

        // 현재 등록된 모든 아두이노 정보를 가져옴
        @JavascriptInterface
        public void fromWeb_allArduino(final String arduinoList){
            if(arduinoList == null){
                Log.e("Javascript-inapp", "fromWeb_allArduino: Argument is null");
                return;
            }

            Log.d("Javascript-inapp", "Bridge2 fromWeb_allArduino:\n" + arduinoList);
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

            // <실행할 함수 추가>
            marker_x = Double.toString(x);
            marker_y = Double.toString(y);
            Editable editable1 = edit_arduinoLng.getEditableText();
            editable1.clear();
            editable1.append(marker_x);
            Editable editable2 = edit_arduinoLat.getEditableText();
            editable2.clear();
            editable2.append(marker_y);
        }
    }
}
