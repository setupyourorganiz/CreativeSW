package com.example.CreativeSW.dto;

public class ArduinoDto {
    public double lng;
    public double lat;
    public String name;
    public ArduinoDto(){

    }
    public ArduinoDto(String name, double lng, double lat){
        this.name = name;
        this.lng = lng;
        this.lat = lat;
    }
}
