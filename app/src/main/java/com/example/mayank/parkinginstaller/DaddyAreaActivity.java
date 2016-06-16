package com.example.mayank.parkinginstaller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DaddyAreaActivity extends AppCompatActivity implements ExistingAreaFragment.OnFragmentInteractionListener
        , RegisterAreaFragment.OnFragmentInteractionListener, View.OnClickListener{


    String TAG_EXISTING = "Existing";
    String TAG_REGISTER = "Register";
    Location currentLocation;
    ArrayList<RegionInfo> regionInfos;
    Button proceed;
    String TAG = "DaddyAreaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daddy_area);
        regionInfos = new ArrayList<RegionInfo>();
        proceed = (Button)findViewById(R.id.proceedDaddyArea);
        proceed.setOnClickListener(this);
        setTitle("Parking Region");
        setupLocation();
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
                getAreaListJSON();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    private void updateAreaList(String jsonList){

        regionInfos.clear();
        JSONArray result = null;
        try {
            JSONObject obj = new JSONObject(jsonList);
            result = obj.getJSONArray(Config.TAG_REGIONS);
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
                String areaName = jo.getString(Config.TAG_REGION_NAME);
                int id = jo.getInt(Config.TAG_REGION_ID);
                regionInfos.add(new RegionInfo(areaName, id));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.daddyContent, ExistingAreaFragment.newInstance(true,regionInfos),TAG_EXISTING).commit();
    }



    private void setupLocation() {
        final ProgressDialog loading = ProgressDialog.show(DaddyAreaActivity.this,"Fetching Location","Wait...",false,false);
        LocationHelper.LocationResult locationResult = new LocationHelper.LocationResult() {
            @Override
            public void gotLocation(final Location location) {
                currentLocation = location;
                if (location != null) {
                    Log.d(TAG,
                            "Got coordinates, congratulations. Longitude = "
                                    + location.getLongitude() + " Latitude = "
                                    + location.getLatitude());
                    getAreaListJSON();
                }
                else {
                    Log.d(TAG, "Location received is null");
                    Toast.makeText(DaddyAreaActivity.this,"Location received is null",Toast.LENGTH_LONG).show();
                }
                loading.dismiss();
            }

        };

        LocationHelper location = new LocationHelper(this);
        boolean found = location.getLocation(this, locationResult);
        if (!found){
            Log.d(TAG,"Could not obtain location");
        }

    }


    private void getAreaListJSON(){
        class getAreaListJSON extends AsyncTask<Void,Void,String> {

            boolean callupdateAreaList = false;
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(DaddyAreaActivity.this,"Fetching Data","Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (callupdateAreaList) {
                    Log.i(TAG, s);
                    updateAreaList(s);
                }else{
                    Toast.makeText(getApplicationContext(), "Error occurred while connecting to server.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                NetworkOps rh = new NetworkOps();
                String s = rh.sendGetRequest(Config.URL_LIST_REGIONS +
                        "?latitude=" + currentLocation.getLongitude() + "&longitude=" + currentLocation.getLongitude());
                callupdateAreaList = true;
                if (s.equals("timeout") || s.equals("error") || s.startsWith("error:")){
                    callupdateAreaList= false;
                }
                return s;
            }
        }
        getAreaListJSON gj = new getAreaListJSON();
        gj.execute();
    }

    public void showRegisterAreaFragment(){
        Fragment fragment = RegisterAreaFragment.newInstance(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.daddyContent, fragment,TAG_REGISTER)
                .commit();
    }

    public void showExistingAreaFragment(){
        Fragment fragment = ExistingAreaFragment.newInstance(true,regionInfos);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.daddyContent, fragment,TAG_EXISTING)
                .commit();
    }

    public void onFragmentInteraction(Uri uri){
    }

    public void onClick(View v){
        if (v.getId() == R.id.proceedDaddyArea){
            ExistingAreaFragment eFragment = (ExistingAreaFragment) getSupportFragmentManager().findFragmentByTag(TAG_EXISTING);
            if (eFragment != null && eFragment.isVisible()) {
                // add your code here
                View view = eFragment.getView();
                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.daddyNameSearch);
                String regionName = autoCompleteTextView.getText().toString();
                Log.i(TAG,"region " + regionName);
                int index = -1;
                for (int i = 0; i < regionInfos.size(); i++){
                    if (regionInfos.get(i).name.equals(regionName)){
                        index = i;
                        break;
                    }
                }
                if (index == -1){
                    Toast.makeText(this,"Please select a valid position",Toast.LENGTH_LONG).show();
                }
                else{
                    RegionInfo regionInfo = regionInfos.get(index);
                    Toast.makeText(this, regionInfo.name + " Region selected",Toast.LENGTH_LONG).show();
                    Intent myIntent = new Intent(DaddyAreaActivity.this, ParkingAreaActivity.class);
                    myIntent.putExtra("regionId", regionInfo.id);
                    myIntent.putExtra("regionName", regionInfo.name);
                    DaddyAreaActivity.this.startActivity(myIntent);
                }
            }
            else{
                RegisterAreaFragment rFragment = (RegisterAreaFragment) getSupportFragmentManager().findFragmentByTag(TAG_REGISTER);
                if (rFragment == null){
                    Toast.makeText(this,"Please Select an Area", Toast.LENGTH_LONG).show();
                    return;
                }
                View view = rFragment.getView();
                EditText editText = (EditText)view.findViewById(R.id.registerName);
                String name = editText.getText().toString();
                LatLng loc = rFragment.getChosenLocation();
                if (name.length() == 0){
                    Toast.makeText(this,"Please Enter a name for new area",Toast.LENGTH_LONG).show();
                }
                else if (loc == null){
                    Toast.makeText(this,"Please choose location for new area",Toast.LENGTH_LONG).show();
                }
                else{
                    HashMap<Object,Object> hashMap = new HashMap<>();
                    hashMap.put("name",name);
                    hashMap.put("latitude",loc.latitude);
                    hashMap.put("longitude",loc.longitude);
                    mapPhone(hashMap);
                }
            }
        }
    }

    private void mapPhone(HashMap<Object,Object> params){
        class mapPhone extends AsyncTask<Void,Void,String> {
            boolean callPhoneMap;
            boolean showServerResp;
            ProgressDialog loading;
            HashMap<Object,Object> params;

            public mapPhone(HashMap<Object,Object> h){
                this.params = h;
            }

            @Override
            protected void onPreExecute() {
                Log.i(TAG,"pre Execute");
                super.onPreExecute();
                loading = ProgressDialog.show(DaddyAreaActivity.this,"Updating Server","Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (callPhoneMap) {
                    Toast.makeText(getApplicationContext(), "Registered.", Toast.LENGTH_LONG).show();
                    Intent myIntent = new Intent(DaddyAreaActivity.this, ParkingAreaActivity.class);
                    JSONObject jsonObject = processResponse(s);
                    if (jsonObject == null){
                        Toast.makeText(DaddyAreaActivity.this,"Invalid response from server",Toast.LENGTH_LONG);
                        return;
                    }
                    try {
                        myIntent.putExtra("regionId", Integer.parseInt(jsonObject.getString("id")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    myIntent.putExtra("regionName",params.get("name").toString());
                    DaddyAreaActivity.this.startActivity(myIntent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Error: " + s, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(Void... p) {
                NetworkOps rh = new NetworkOps();
                Tuple tup = rh.sendPostRequest(Config.URL_REGISTER_REGION, params);
                callPhoneMap = false;
                showServerResp = false;
                if (tup.getResponseCode() == 201){
                    callPhoneMap = true;
                }
                return tup.response;
            }
        }
        mapPhone mp = new mapPhone(params);
        mp.execute();
    }

    public JSONObject processResponse(String s){
        try {
            JSONObject jsonObject = new JSONObject(s);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
