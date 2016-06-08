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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ParkingAreaActivity extends AppCompatActivity implements View.OnClickListener
            , ExistingAreaFragment.OnFragmentInteractionListener, RegisterAreaFragment.OnFragmentInteractionListener{

    String TAG_EXISTING = "Existing";
    String TAG_REGISTER = "Register";
    ArrayList<RegionInfo> regionInfos;
    int regionIdSelected;
    String regionNameSelected;
    Button proceed;
    String TAG = "ParkingAreaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_area);
        Intent intent = getIntent();
        regionIdSelected = intent.getIntExtra("regionId",0);
        Log.i(TAG,"iddddddddddddddddddddddddddddddd  "+regionIdSelected);
        regionNameSelected = intent.getStringExtra("regionName");
        proceed = (Button)findViewById(R.id.proceedArea);
        proceed.setOnClickListener(this);
        regionInfos = new ArrayList<RegionInfo>();
        setTitle(regionNameSelected);
        getAreaListJSON();
    }

    public void onClick(View v){
        if (v.getId() == R.id.proceedArea){
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
                    Intent myIntent = new Intent(ParkingAreaActivity.this, QRActivity.class);
                    myIntent.putExtra("regionId",regionIdSelected);
                    myIntent.putExtra("regionName",regionNameSelected);
                    myIntent.putExtra("areaId", regionInfo.id);
                    myIntent.putExtra("areaName", regionInfo.name);
                    ParkingAreaActivity.this.startActivity(myIntent);
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
                    hashMap.put("region",regionIdSelected);

                    mapPhone(hashMap);
                }
            }
        }
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
            result = new JSONArray(jsonList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (result == null){
            Toast.makeText(this,"Sensor list is empty",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            for(int i = 0; i<result.length(); i++){
                JSONObject jo = result.getJSONObject(i).getJSONObject(Config.TAG_AREA);
                String areaName = jo.getString(Config.TAG_REGION_NAME);
                int id = jo.getInt(Config.TAG_REGION_ID);
                regionInfos.add(new RegionInfo(areaName, id));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.areaContent, ExistingAreaFragment.newInstance(false,regionInfos),TAG_EXISTING).commit();
    }


    private void getAreaListJSON(){
        class getAreaListJSON extends AsyncTask<Void,Void,String> {

            boolean callupdateAreaList = false;
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ParkingAreaActivity.this,"Fetching Data","Wait...",false,false);
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
                String s = rh.sendGetRequest(Config.URL_LIST_AREA + regionIdSelected);
                callupdateAreaList = true;
                if (s == "timeout" || s == "error"){
                    callupdateAreaList= false;
                }
                return s;
            }
        }
        getAreaListJSON gj = new getAreaListJSON();
        gj.execute();
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
                loading = ProgressDialog.show(ParkingAreaActivity.this,"Updating Server","Wait...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (callPhoneMap) {
                    Toast.makeText(getApplicationContext(), "Registered.", Toast.LENGTH_LONG).show();
                    Intent myIntent = new Intent(ParkingAreaActivity.this, QRActivity.class);
                    JSONObject jsonObject = processResponse(s);
                    if (jsonObject == null){
                        Toast.makeText(ParkingAreaActivity.this, "Invalid response from server", Toast.LENGTH_LONG).show();
                        return;
                    }
                    myIntent.putExtra("regionName",regionNameSelected);
                    myIntent.putExtra("regionId",regionIdSelected);
                    try {
                        myIntent.putExtra("areaId", Integer.parseInt(jsonObject.getString("id")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    myIntent.putExtra("areaName",params.get("name").toString());
                    ParkingAreaActivity.this.startActivity(myIntent);
                }
                else if (showServerResp){
                    Toast.makeText(ParkingAreaActivity.this,s,Toast.LENGTH_LONG ).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Error occurred while connecting to server.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(Void... p) {
                NetworkOps rh = new NetworkOps();
                Tuple tup = rh.sendPostRequest(Config.URL_REGISTER_AREA, params);
                callPhoneMap = false;
                showServerResp = false;
                if (tup.getResponseCode() == 201){
                    callPhoneMap = true;
                }
                else if (tup.getResponseCode() == 400){
                    showServerResp = false;
                }
                return tup.response;
            }
        }
        mapPhone mp = new mapPhone(params);
        mp.execute();
    }


    public void showRegisterAreaFragment(){
        Fragment fragment = RegisterAreaFragment.newInstance(false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.areaContent, fragment,TAG_REGISTER)
                .commit();
    }

    public void showExistingAreaFragment(){
        Fragment fragment = ExistingAreaFragment.newInstance(false,regionInfos);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.areaContent, fragment,TAG_EXISTING)
                .commit();
    }

    public void onFragmentInteraction(Uri uri){
    }



}
