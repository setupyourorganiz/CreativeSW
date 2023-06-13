package com.example.final_project.dto;

public class PathArduinoDto {
    public ArduinoDto arduino;
    public double direction;
    public PathArduinoDto(){

    }
    public PathArduinoDto(ArduinoDto arduino, double direction){
        this.arduino = arduino;
        this.direction = direction;
    }
}
