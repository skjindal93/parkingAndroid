package com.example.mayank.parkinginstaller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

class RegionInfo implements Parcelable{
    String name;
    int id;

    public RegionInfo(String name, int id){
        this.name = name;
        this.id = id;
    }

    public RegionInfo(Parcel in) {
        super();
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        name = in.readString();
        id = in.readInt();
    }

    public static final Parcelable.Creator<RegionInfo> CREATOR = new Parcelable.Creator<RegionInfo>() {
        public RegionInfo createFromParcel(Parcel in) {
            return new RegionInfo(in);
        }

        public RegionInfo[] newArray(int size) {
            return new RegionInfo[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(id);
    }

}

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExistingAreaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExistingAreaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExistingAreaFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "isDaddyArea";
    private static final String ARG_PARAM2 = "regionInfoList";


    private boolean isDaddyArea;
    ArrayList<RegionInfo> regionInfoList;
    AutoCompleteTextView daddyNameSearch = null;


    private OnFragmentInteractionListener mListener;

    public ExistingAreaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ExistingAreaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExistingAreaFragment newInstance(boolean param1, ArrayList<RegionInfo> regionInfoList) {
        ExistingAreaFragment fragment = new ExistingAreaFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, param1);
        args.putParcelableArrayList(ARG_PARAM2,regionInfoList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isDaddyArea = getArguments().getBoolean(ARG_PARAM1);
            regionInfoList = getArguments().getParcelableArrayList(ARG_PARAM2);
        }
        else{
            Toast.makeText(getActivity(),"Arguments null, error!", Toast.LENGTH_LONG).show();
        }
    }

    public void onClick(View view){
        if (view.getId() == R.id.registerNewArea){
            if (isDaddyArea) {
                ((DaddyAreaActivity) getActivity()).showRegisterAreaFragment();
            }
            else{
                ((ParkingAreaActivity)getActivity()).showRegisterAreaFragment();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_existing_area, container, false);
        Button registerNewArea = (Button)view.findViewById(R.id.registerNewArea);
        registerNewArea.setOnClickListener(this);
        daddyNameSearch = (AutoCompleteTextView)view.findViewById(R.id.daddyNameSearch);

        CustomAdaptor adapter = new CustomAdaptor(getActivity(),R.layout.region_list_item,regionInfoList);

        daddyNameSearch.setAdapter(adapter);
        daddyNameSearch.setThreshold(1);
        daddyNameSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        });
        return view;
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
