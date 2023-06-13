package com.example.final_project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.dto.ArduinoDto;
import com.example.final_project.dto.PathArduinoDto;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebViewActivity extends AppCompatActivity {
    private WebView webView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subactivity_main);

        Button imageButton = findViewById(R.id.next_layout);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        webView = findViewById(R.id.webview);

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

        webView.loadUrl("http://3.35.191.211:8080");

        webView.addJavascriptInterface(new WebViewActivity.Bridge(this), "Bridge");

        Button regist = findViewById(R.id.regist);
        Button callin = findViewById(R.id.call_in);

        Spinner wholearduino = findViewById(R.id.whole_arduino);
        Spinner inroutearduino = findViewById(R.id.in_route_arduino);

        EditText name = findViewById(R.id.name);
        EditText lat = findViewById(R.id.lat);
        EditText lng = findViewById(R.id.lng);

        String Name = name.getText().toString();    // 이름 입력을 Name에 저장
        String Lat = lat.getText().toString();      // 위도 입력을 Lat에 저장
        String Lng = lng.getText().toString();      // 경도 입력을 Lng에 저장

        // 스피너(리스트박스) 사용을 위한 설정 및 함수들
        String[] strings = {"item1", "item2"};  // 임시사용한 배열 변수
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,android.R.layout.simple_spinner_item, strings
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        wholearduino.setAdapter(adapter);
        inroutearduino.setAdapter(adapter);
        wholearduino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                // 선택되면 사용할 함수 작성
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){
                // 선택되지 않았을 때
            }
        });
        inroutearduino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
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
        regist.setOnClickListener((new View.OnClickListener(){
            public void onClick(View v){
                webView.loadUrl("javascript:toWeb_addArduino(name, x, y)");
            }
        }));

        // 아두이노 리스트 불러오기 버튼
        callin.setOnClickListener((new View.OnClickListener(){
            public void onClick(View v){
                webView.loadUrl("javascript:toWeb_allArduino()");
            }
        }));
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
                Log.e("Javascript", "fromWeb_allArduino: Argument is null");
                return;
            }
            Log.d("Javascript", "fromWeb_allArduino:\n" + arduinoList);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ArduinoDto[] arduinoDtoList = objectMapper.readValue(arduinoList, ArduinoDto[].class);

                // TARGET_DEVICE_NAME에 들어있는 이름과 같은 기기를 발견하면 해당 기기와 연결


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
        }
    }
}
