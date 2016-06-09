package com.example.mayank.parkinginstaller;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
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

import java.io.File;
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
    Switch testingMode;
    int PLACE_PICKER_REQUEST = 1;
    LatLng currentLoc;
    boolean inTestingMode;

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
        testingMode = (Switch)findViewById(R.id.testingMode);
        inTestingMode = false;
        testingMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sAdapter.setSelectedPort(-1);
                sAdapter.notifyDataSetChanged();
                if (isChecked) {
                    // The toggle is enabled
                    inTestingMode = true;
                    getLoc.setEnabled(false);
                    addSensor.setEnabled(false);
                } else {
                    // The toggle is disabled
                    inTestingMode = false;
                    getLoc.setEnabled(true);
                    addSensor.setEnabled(true);
                }
            }
        });


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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sensor_menu, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
        SensorStatus sst = sAdapter.getItem(position);
        if (inTestingMode){
            if (!sst.status){
                int sp = Integer.parseInt(sst.sensorName);
                Intent myIntent = new Intent(SensorActivity.this, SensorDetailActivity.class);
                myIntent.putExtra("piId", piId); //Raspberry Pi ID
                Log.i(TAG,"-------- " + sp);
                myIntent.putExtra("piPort",sp);
                SensorActivity.this.startActivity(myIntent);
            }
        }
        else {
            if (sst.status) {
                int sp = Integer.parseInt(sst.sensorName);
                sAdapter.setSelectedPort(sp);
                sAdapter.notifyDataSetChanged();
                //v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
            }
        }

    }

    public void onClick(View v){
        if (v.getId() == R.id.getLocation){
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                Toast.makeText(this,"Google Play Services not available", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                Toast.makeText(this,"Google Play Services not available", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        else if (v.getId() == R.id.addSensor){
            int sp = sAdapter.getSelectedPort();
            if (sp == -1){
                Toast.makeText(this,"Select an empty port first",Toast.LENGTH_LONG).show();
            }
            else if (currentLoc == null){
                Toast.makeText(this,"Please update current location",Toast.LENGTH_LONG).show();
            }
            else{
                Intent intent = new Intent(SensorActivity.this,SensorQRActivity.class);
                intent.putExtra("latitude",currentLoc.latitude);
                intent.putExtra("longitude",currentLoc.longitude);
                intent.putExtra("pi_port",sp);
                intent.putExtra("pi",piId);
                SensorActivity.this.startActivity(intent);
                finish();
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                LatLng coord = place.getLatLng();
                locationBox.setText("Lat, Long: " + Config.round(coord.latitude,2) + ", " + Config.round(coord.longitude,2));
                currentLoc = place.getLatLng();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // User chose the "Settings" item, show the app settings UI...
                getJSON();
                sAdapter.setSelectedPort(-1);
                return true;

            case R.id.action_portmap:
                //TODO: Show portmap structure on press

                Dialog builder = new Dialog(this);
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //nothing;
                    }
                });

                ImageView imageView = new ImageView(this);
                Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + getResources().getResourcePackageName(R.drawable.portmap)
                        + '/' + getResources().getResourceTypeName(R.drawable.portmap) + '/' + getResources().getResourceEntryName(R.drawable.portmap) );
                imageView.setImageURI(imageUri);
                builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                builder.show();

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    private void showSensors(String jsonList){

        List<SensorStatus> list = sAdapter.getList();
        list.clear();
        JSONArray result = null;
        try {
            JSONObject obj = new JSONObject(jsonList);
            result = obj.getJSONArray(Config.TAG_PORTLIST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (result == null){
            Toast.makeText(this,"Sensor list is empty",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            for(int i = 0; i<result.length(); i++){
                JSONObject jo = result.getJSONObject(i);
                String port = jo.getString(Config.TAG_PORT);
                boolean status = !jo.getBoolean(Config.TAG_USED);
                SensorStatus sst = new SensorStatus(port, status);
//                Log.i(TAG,port + " " + status);
                list.add(sst);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"Notifying SensorArrayAdapter, list size is " + list.size());
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
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                NetworkOps rh = new NetworkOps();
                String s = rh.sendGetRequest(Config.URL_SENSOR_PI + "?pi=" + 1);  /// TODO: REMOVE HARDCODED PI ID
                callShowSensors = true;
                if (s == "timeout" || s == "error" || s.startsWith("error: ")){
                    callShowSensors= false;
                }
                return s;
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }


}
