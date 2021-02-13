package org.cdortona.tesi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomAdapterView extends ArrayAdapter<DevicesScannedModel>{

    private Context mContext;
    //private ArrayList<DevicesScannedModel> mList;

     CustomAdapterView(@NonNull Context context, int resource, @NonNull ArrayList<DevicesScannedModel> objects) {
        super(context, resource, objects);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View customView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_view_layout, parent, false);

        TextView customText1 = customView.findViewById(R.id.text1);
        TextView customText2 = customView.findViewById(R.id.text2);
        TextView customText3 = customView.findViewById(R.id.text3);

        DevicesScannedModel device = getItem(position);
        customText1.setText(device.getDeviceName());
        customText2.setText(device.getBleAddress());
        customText3.setText(Integer.toString(device.getRssi()));

        return customView;
    }

}

