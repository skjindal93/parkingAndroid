package com.example.mayank.parkinginstaller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mayank on 07-06-2016.
 */
public class CustomAdaptor extends ArrayAdapter<RegionInfo>{
    private String TAG = "CustomAdaptor";
    private ArrayList<RegionInfo> regionInfo;
    private ArrayList<RegionInfo> regionInfoClone;
    private ArrayList<RegionInfo> suggestions;
    private int viewResourceId;

    public CustomAdaptor(Context context, int viewResourceId, ArrayList<RegionInfo> regionInfo){
        super(context,viewResourceId,regionInfo);
        this.regionInfo = regionInfo;
        this.regionInfoClone = (ArrayList<RegionInfo>) regionInfo.clone();
        this.suggestions = new ArrayList<RegionInfo>();
        this.viewResourceId = viewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        RegionInfo rInfo = regionInfo.get(position);
        if (rInfo != null) {
            TextView customerNameLabel = (TextView) v.findViewById(R.id.regionListName);
            if (customerNameLabel != null) {
//              Log.i(MY_DEBUG_TAG, "getView Customer Name:"+customer.getName());
                customerNameLabel.setText(rInfo.name);
            }
        }
        return v;
    }

    @Override


    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            String str = ((RegionInfo)(resultValue)).name;
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null) {
                suggestions.clear();
                for (RegionInfo rInfo : regionInfoClone) {
                    if(rInfo.name.toLowerCase().startsWith(constraint.toString().toLowerCase())){
                        suggestions.add(rInfo);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<RegionInfo> filteredList = (ArrayList<RegionInfo>) results.values;
            if(results != null && results.count > 0) {
                clear();
                for (RegionInfo c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}
