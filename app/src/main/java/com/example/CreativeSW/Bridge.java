package com.example.CreativeSW;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.example.CreativeSW.dto.ArduinoDto;
import com.example.CreativeSW.dto.PathArduinoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
public class Bridge{
    Context mContext;
    public Bridge(Context context){
        mContext = context;
    }

    // 현재 등록된 모든 아두이노 정보를 가져옴
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //whole_arudino.clear();
                    //whole_arudino.addAll(arr2arrList_arduinoDto(arduinoDtoList));
                }
            });

        } catch (Exception e) {
            Log.e("Javascript-inapp", e.getMessage());
            e.printStackTrace();
        }
        //update_spin_wholeArduino(arduinoDtoList2StrList(whole_arudino));
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