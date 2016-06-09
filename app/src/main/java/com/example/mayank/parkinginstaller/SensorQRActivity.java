package com.example.mayank.parkinginstaller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.UUID;

public class SensorQRActivity extends AppCompatActivity implements View.OnClickListener{

    TextView sensorDetails;
    String sensorId;
    HashMap<Object,Object> args;
    String TAG = "SensorQRActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_qr);
        setTitle("Scan Sensor QR");
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", -1);
        double longitude = intent.getDoubleExtra("longitude", -1);
        String pi_id = intent.getStringExtra("pi");
        int pi_port = intent.getIntExtra("pi_port",-1);
        args = new HashMap<Object,Object>();
        args.put("latitude",latitude);
        args.put("longitude",longitude);
        args.put("pi_port",pi_port);
        args.put("pi",pi_id);

        sensorDetails = (TextView)findViewById(R.id.sensor1234);
        ImageButton qr = (ImageButton)findViewById(R.id.sensorImageButton);
        Button proceed = (Button)findViewById(R.id.updateSensorDetails);
        detailsBoxMsg(pi_id,pi_port,latitude,longitude);
        sensorMsgUpdate();
        qr.setOnClickListener(this);
        proceed.setOnClickListener(this);
    }

    public void onClick(View view){
        if (view.getId() == R.id.updateSensorDetails){
            // Update Button
                args.put("qr",sensorId);
                registerSensor(args);
        }
        else if (view.getId() == R.id.sensorImageButton){
            // Scan Sensor QR
            try {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
                startActivityForResult(intent, 0);
            } catch (Exception e) {
                Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
                startActivity(marketIntent);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                sensorId = contents;
                sensorMsgUpdate();
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
    }



    public void detailsBoxMsg(String piId, int pi_port, double latitude, double longitude){
        sensorDetails.setText("Pi ID: " + piId + "\n");
        sensorDetails.append("Pi Port: " + pi_port + "\n");
        sensorDetails.append("Latitude: " + latitude + "\n");
        sensorDetails.append("Longitude: " + longitude + "\n");
    }

    public void sensorMsgUpdate(){
        if (sensorId == null || sensorId.length() == 0){
            sensorDetails.append("Press QR to scan get Sensor QR's ID");
        }
        else{
            sensorDetails.append("Sensor Id: " + sensorId);
        }
    }

    private void registerSensor(HashMap<Object,Object> params){
        class mapPhone extends AsyncTask<Void,Void,String> {
            boolean callPhoneMap;
            ProgressDialog loading;
            HashMap<Object,Object> params;
            String error;

            public mapPhone(HashMap<Object,Object> h){
                this.params = h;
            }

            @Override
            protected void onPreExecute() {
                Log.i(TAG,"pre Execute");
                super.onPreExecute();
                loading = ProgressDialog.show(SensorQRActivity.this,"Updating Server","Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (callPhoneMap) {
                    Toast.makeText(getApplicationContext(), "New Sensor Added", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SensorQRActivity.this, SensorActivity.class);
                    intent.putExtra("piId", params.get("pi").toString());
                    SensorQRActivity.this.startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Error! "  + s, Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


}



