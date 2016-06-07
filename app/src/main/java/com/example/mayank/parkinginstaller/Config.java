package com.example.mayank.parkinginstaller;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Mayank on 13-05-2016.
 */
public class Config {
    public static String server = "http://locatr.cse.iitd.ac.in:8001/";

    public static String api_phoneMap = server + "installation/raspberryPhoneMap/";
    public static String URL_NEW_SENSOR = server + "installation/newSensor/";
    public static String URL_REGISTER_REGION = server + "parking/registerRegion/";
    public static String URL_SENSOR_PI = server + "parking/sensorPorts/";
    public static String URL_SENSOR_DETAIL = server + "parking/sensorDetail/";
    public static String URL_LIST_REGIONS = server + "parking/regions/";
    public static String TAG_PORTLIST = "ports";
    public static String TAG_REGIONS = "regions";
    public static String TAG_REGION_NAME = "name";
    public static String TAG_REGION_ID = "id";
    public static String TAG_PORT = "pi_port";
    public static String TAG_USED = "used";


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

