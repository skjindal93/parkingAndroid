package com.example.mayank.parkinginstaller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SensorDetailActivity extends AppCompatActivity {

    int piId;
    int piPort;
    String TAG = "SensorDetailActivity";
    TextView sensorDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_detail);
        Intent intent = getIntent();
        piId = Integer.parseInt(intent.getStringExtra("piId"));
        piPort = intent.getIntExtra("piPort",-1);
        sensorDetail = (TextView)findViewById(R.id.sensorDetail);
        sensorDetail.setText(generateText(piId,piPort,89.99,99.99,true));

    }

    private String generateText(int piId, int piPort, double lati, double longi, boolean occupied){
        String ret = "";
        ret = ret + "Pi Id: " + piId + "\n";
        ret = ret + "Pi Port: " + piPort + "\n";
        ret = ret + "Latitude: " + lati + "\n";
        ret = ret + "Longitude: " + longi + "\n";
        ret = ret + "Occupied: " + occupied;
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sensor_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // User chose the "Settings" item, show the app settings UI...
                getJSON();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void showSensors(String jsonList){
//        sensorDetail.setText(generateText(piId,piPort,lati,longi,occupied));

    }


    private void getJSON(){
        class GetJSON extends AsyncTask<Void,Void,String> {

            boolean showSensorDetail = false;
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(SensorDetailActivity.this,"Fetching Data","Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (showSensorDetail) {
                    Log.i(TAG, s);
                    showSensors(s);
                }else{
                    Toast.makeText(getApplicationContext(), "Error occurred while connecting to server.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                NetworkOps rh = new NetworkOps();
                String s = rh.sendGetRequest(Config.URL_SENSOR_PI + "?pi=" + piId + "&pi_port=" + piPort);  /// TODO: UPDATE URL
                showSensorDetail = true;
                if (s == "timeout" || s == "error"){
                    showSensorDetail= false;
                }
                return s;
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }

}
