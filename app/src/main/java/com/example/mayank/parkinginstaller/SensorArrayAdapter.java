package com.example.mayank.parkinginstaller;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mayank on 24-05-2016.
 */

class SensorStatus{
    String sensorName;
    String echoPort;
    boolean status;

    public SensorStatus(String sensorName, String echoPort, boolean status){
        this.sensorName = sensorName;
        this.echoPort = echoPort;
        this.status = status;
    }

}

public class SensorArrayAdapter extends ArrayAdapter<SensorStatus> {
    String TAG = "SensorArrayAdapter";
    private final Context context;
    private List<SensorStatus> list;
    private int selectedPort;

    public SensorArrayAdapter(Context context, List<SensorStatus> values) {
        super(context, R.layout.list_item, values);
        this.context = context;
        this.list = values;
        selectedPort = -1;

    }

    public List<SensorStatus> getList(){
        return this.list;
    }

    public void setSelectedPort(int sp){
        this.selectedPort = sp;
    }

    public int getSelectedPort(){
        return this.selectedPort;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item, null);
        }
        SensorStatus ssInfo = getItem(position);
        if (ssInfo != null){

            TextView sensorPort = (TextView)v.findViewById(R.id.sensorPort);
            TextView sensorStatus = (TextView)v.findViewById(R.id.statusColor);
            sensorPort.setText("Port No: " + ssInfo.sensorName + ", Echo Port: " + ssInfo.echoPort);
            if (Integer.parseInt(ssInfo.sensorName) == selectedPort){
                v.setBackgroundResource(R.color.pressed_color);
            }
            else{
                v.setBackgroundResource(R.color.transparent);
            }
            if (ssInfo.status) {
                sensorStatus.setBackgroundResource(R.color.green);
            }
            else{
                sensorStatus.setBackgroundResource(R.color.red);
            }

        }
        return v;
    }

}
