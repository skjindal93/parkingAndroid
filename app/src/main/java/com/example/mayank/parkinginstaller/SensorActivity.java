package com.example.mayank.parkinginstaller;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SensorActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, Button.OnClickListener {

    List<SensorStatus> sensors;
    String piId = null;
    String TAG = "SensorActivity";
    SensorArrayAdapter sAdapter;
    ListView sensorListView;
    TextView locationBox;
    Button getLoc;
    Button addSensor;
    int PLACE_PICKER_REQUEST = 1;
    LatLng currentLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        sensorListView = (ListView) findViewById(R.id.sensorListView);
        getLoc = (Button)findViewById(R.id.getLocation);
        addSensor = (Button)findViewById(R.id.addSensor);
        locationBox = (TextView)findViewById(R.id.locationBox);
        getLoc.setOnClickListener(this);
        addSensor.setOnClickListener(this);
        sensors = new ArrayList<>();
        currentLoc = null;
        sAdapter = new SensorArrayAdapter(this, sensors);
        sensorListView.setAdapter(sAdapter);
        sensorListView.setOnItemClickListener(this);
        Intent intent = getIntent();
        piId = intent.getStringExtra("piId");
        TextView heading = (TextView)findViewById(R.id.piName);
        heading.append(piId);
        getJSON();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
        SensorStatus sst = sAdapter.getItem(position);
        Log.i(TAG,"heheee  " + sst.status);
        int sp = Integer.parseInt(sst.sensorName);
        sAdapter.setSelectedPort(sp);
        sAdapter.notifyDataSetChanged();
        if (sst.status){

            Log.i(TAG,"huuuuuuu  " + sp);

            //v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
        }
    }

    public void onClick(View v){
        if (v.getId() == R.id.getLocation){
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                Toast.makeText(this,"Google Play Services not available", Toast.LENGTH_SHORT);
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                Toast.makeText(this,"Google Play Services not available", Toast.LENGTH_SHORT);
                e.printStackTrace();
            }
        }
        else if (v.getId() == R.id.addSensor){
            int sp = sAdapter.getSelectedPort();
            if (sp == -1){
                Toast.makeText(this,"Select an empty port first",Toast.LENGTH_LONG);
            }
            else if (currentLoc == null){
                Toast.makeText(this,"Please update current location",Toast.LENGTH_LONG);
            }
            else{
                HashMap<String,String> h = new HashMap<String,String>();
                h.put("pi",piId);
                h.put("pi_port",Integer.toString(sp));
                h.put("latitude",Double.toString(currentLoc.latitude));
                h.put("longitude",Double.toString(currentLoc.longitude));
                registerSensor(h);
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                locationBox.setText(place.getLatLng().toString());
                currentLoc = place.getLatLng();
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }


    private void showSensors(String jsonList){

        List<SensorStatus> list = sAdapter.getList();
        list.clear();
        JSONArray result = new JSONArray();
        try {
            result = new JSONArray(jsonList);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            for(int i = 0; i<result.length(); i++){
                JSONObject jo = result.getJSONObject(i);
                String port = jo.getString(Config.TAG_PORT);
                boolean status = jo.getBoolean(Config.TAG_OCCUPIED);
                SensorStatus sst = new SensorStatus(port, status);
//                Log.i(TAG,port + " " + status);
                list.add(sst);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"Notifying ViewPager, list size is " + list.size());
        sAdapter.notifyDataSetChanged();
    }



    private void getJSON(){
        class GetJSON extends AsyncTask<Void,Void,String> {

            boolean callShowSensors = false;
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(SensorActivity.this,"Fetching Data","Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (callShowSensors) {
                    Log.i(TAG, s);
                    showSensors(s);
                }else{
                    Toast.makeText(getApplicationContext(), "Error occurred while connecting to server.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                NetworkOps rh = new NetworkOps();
                String s = rh.sendGetRequest(Config.URL_SENSOR_PI + "?pi=" + 1);  /// TODO: REMOVE HARDCODED PI ID
                callShowSensors = true;
                if (s == "timeout" || s == "error"){
                    callShowSensors= false;
                }
                return s;
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }

    private void registerSensor(HashMap<String,String> params){
        class mapPhone extends AsyncTask<Void,Void,String> {
            boolean callPhoneMap;
            ProgressDialog loading;
            HashMap<String,String> params;

            public mapPhone(HashMap<String,String> h){
                this.params = h;
            }

            @Override
            protected void onPreExecute() {
                Log.i(TAG,"pre Execute");
                super.onPreExecute();
                loading = ProgressDialog.show(SensorActivity.this,"Updating Server","Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (callPhoneMap) {
                    Toast.makeText(getApplicationContext(), "Response: " + s, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Error occurred while connecting to server.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(Void... p) {
                NetworkOps rh = new NetworkOps();
                Tuple tup = rh.sendPostRequest(Config.URL_NEW_SENSOR, params);
                callPhoneMap = false;
                if (tup.getResponseCode() == 201){
                    callPhoneMap = true;
                }
                return tup.response;
            }
        }
        mapPhone mp = new mapPhone(params);
        mp.execute();
    }

}
