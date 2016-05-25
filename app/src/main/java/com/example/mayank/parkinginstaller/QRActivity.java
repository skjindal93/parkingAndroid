package com.example.mayank.parkinginstaller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QRActivity extends AppCompatActivity implements View.OnClickListener {

    TextView details;
    TextView detailsHeading;
    String TAG = "QRActivity";
    String piId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        ImageButton qr = (ImageButton)findViewById(R.id.imageButton);
        Button proceed = (Button)findViewById(R.id.proceed);
        detailsHeading = (TextView)findViewById(R.id.textView2);
        details = (TextView)findViewById(R.id.details);

        qr.setOnClickListener(this);
        proceed.setOnClickListener(this);

    }

    public void onClick(View v){
        if (v.getId() == R.id.imageButton){
            try {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
                startActivityForResult(intent, 0);
            } catch (Exception e) {
                Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
                startActivity(marketIntent);
            }
        }else if (v.getId() == R.id.proceed){
            if (piId != null){
                String MAC = getMacAddress();
                HashMap<String,String> h = new HashMap<String,String>();
                h.put("pi",piId);
                h.put("phone_mac",MAC);
                Log.i(TAG,"Proceed pressed");
                mapPhone(h);
            }
            else{
                Toast.makeText(getApplicationContext(), "Scan QR first to get R-Pi details", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                details.setText("Id of scanned R-pi: " + contents);
                piId = contents;
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
    }

    public String getMacAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }


    private void mapPhone(HashMap<String,String> params){
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
                loading = ProgressDialog.show(QRActivity.this,"Updating Server","Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (callPhoneMap) {
                    Intent myIntent = new Intent(QRActivity.this, SensorActivity.class);
                    myIntent.putExtra("piId", piId); //Raspberry Pi ID
                    QRActivity.this.startActivity(myIntent);
                }else{
                    Toast.makeText(getApplicationContext(), "Error occurred while connecting to server.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(Void... p) {
                NetworkOps rh = new NetworkOps();
                Tuple tup = rh.sendPostRequest(Config.api_phoneMap, params);
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
