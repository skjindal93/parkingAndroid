package com.example.mayank.parkinginstaller;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.util.StringBuilderPrinter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Mayank on 13-05-2016.
 */

class Tuple{
    int responseCode;
    String response;

    public Tuple(int rc, String rp){
        this.responseCode = rc;
        this.response = rp;
    }

    public int getResponseCode(){
        return this.responseCode;
    }

    public String getResponse(){
        return this.response;
    }
}

public class NetworkOps {

    String TAG = "NetworkOps";

    //Method to send httpPostRequest
    //This method is taking two arguments
    //First argument is the URL of the script to which we will send the request
    //Other is an HashMap with name value pairs containing the data to be send with the request
    public Tuple sendPostRequest(String requestURL,
                                  HashMap<Object, Object> postDataParams) {
        //Creating a URL
        String logString = "POST Input: ";
        Set s = postDataParams.keySet();
        Iterator iter = s.iterator();
        while (iter.hasNext()){
            Object object = iter.next();
            logString += object.toString() +": " + postDataParams.get(object) + ", " ;
        }
        Log.i(TAG,logString);
        URL url;
        //StringBuilder object to store the message retrieved from the server
        Tuple tup = null;
        int responseCode = 404;
        HttpURLConnection conn= null;
        try {
            //Initializing Url
            url = new URL(requestURL);

            //Creating an httmlurl connection
            conn = (HttpURLConnection) url.openConnection();

            //Configuring connection properties
            conn.setReadTimeout(2000);
            conn.setConnectTimeout(2000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            //Creating an output stream
            OutputStream os = conn.getOutputStream();

            //Writing parameters to the request
            //We are using a method getPostDataString which is defined below
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            responseCode = conn.getResponseCode();



            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String response;
            //Reading server response
            while ((response = br.readLine()) != null){
                sb.append(response);
            }
            Log.i(TAG,"POST Output: Reponse Code: " + responseCode + " " + sb.toString());
            tup = new Tuple(responseCode,sb.toString());

        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            if (conn != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String response;
                //Reading server response
                try {
                    while ((response = br.readLine()) != null){
                        sb.append(response);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            Log.i(TAG,"POST Error Output: Reponse Code: " + responseCode + " " + sb.toString());
            e.printStackTrace();
            tup = new Tuple(responseCode,sb.toString());
        }
        return tup;
    }

    public String sendGetRequest(String requestURL){
        return sendGetRequest(requestURL, "");
    }

    public String sendGetRequest(String requestURL, String id){
        Log.i(TAG,"Get Request sent to " + requestURL + id);
        StringBuilder sb =new StringBuilder();
        HttpURLConnection con = null;
        BufferedReader errorReader = null;
        try {
            URL url = new URL(requestURL+id);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(2000);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String s;
            while((s=bufferedReader.readLine())!=null){
                sb.append(s+"\n");
            }
        }catch(SocketTimeoutException e){
            e.printStackTrace();
            String ans = sb.toString();
            Log.i(TAG,"GET output is " + ans);
            return "timeout";
        }
        catch (Exception e){
            errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            e.printStackTrace();
            sb = new StringBuilder();
            if (errorReader != null) {
                String response;
                //Reading server response
                try {
                    while ((response = errorReader.readLine()) != null){
                        sb.append(response);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            Log.i(TAG,"GET Error Output: Response: "+ sb.toString());
            return "error: " + sb.toString() ;
        }
        String ans = sb.toString();
        Log.i(TAG,"GET output is " + ans);
        return ans;
    }


    private String getPostDataString(HashMap<Object, Object> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<Object, Object> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey().toString(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
        }

        return result.toString();
    }
}
