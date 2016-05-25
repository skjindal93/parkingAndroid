package com.example.mayank.parkinginstaller;

/**
 * Created by Mayank on 13-05-2016.
 */
public class Config {
    public static String server = "http://locatr.cse.iitd.ac.in:8001/";

    public static String api_phoneMap = server + "installation/raspberryPhoneMap/";
    public static String URL_NEW_SENSOR = server + "/installation/newSensor/";
    public static String URL_SENSOR_PI = server + "parking/sensorPorts/";

    public static String TAG_PORT = "pi_port";
    public static String TAG_OCCUPIED = "occupied";
}
