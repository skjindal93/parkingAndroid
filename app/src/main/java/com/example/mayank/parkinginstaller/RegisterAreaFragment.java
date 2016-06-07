package com.example.mayank.parkinginstaller;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisterAreaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegisterAreaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterAreaFragment extends Fragment implements View.OnClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "isDaddyArea";


    private boolean isDaddyArea;
    int PLACE_PICKER_REQUEST = 2;
    EditText registerLatiLongi = null;
    LatLng chosenLocation;

    private OnFragmentInteractionListener mListener;

    public RegisterAreaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment RegisterAreaFragment.
     */
    public static RegisterAreaFragment newInstance(boolean param1) {
        RegisterAreaFragment fragment = new RegisterAreaFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isDaddyArea = getArguments().getBoolean(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_register_area, container, false);
        Button showExistingAreaFragment = (Button)view.findViewById(R.id.existingArea);
        showExistingAreaFragment.setOnClickListener(this);
        Button registerChooseLoc = (Button)view.findViewById(R.id.registerChooseLoc);
        registerChooseLoc.setOnClickListener(this);
        registerLatiLongi = (EditText)view.findViewById(R.id.registerLatiLongi);
        registerLatiLongi.setEnabled(false);
        return view;
    }

    public void onClick(View view){
        if (view.getId() == R.id.existingArea){
            if (isDaddyArea) {
                ((DaddyAreaActivity) getActivity()).showExistingAreaFragment();
            }
            else{
                ((ParkingAreaActivity) getActivity()).showExistingAreaFragment();
            }
        }
        else if (view.getId() == R.id.registerChooseLoc){
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                Toast.makeText(getActivity(),"Google Play Services not available", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                Toast.makeText(getActivity(),"Google Play Services not available", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == getActivity().RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getActivity());
                chosenLocation = place.getLatLng();
                registerLatiLongi.setText("Latitude, Longitude: " + Config.round(chosenLocation.latitude,2) + ", " + Config.round(chosenLocation.longitude,2));
            }
        }
    }

    public LatLng getChosenLocation(){
        return this.chosenLocation;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
