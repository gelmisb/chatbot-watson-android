package com.example.vmac.WatBot;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.awareness.state.Weather;

import java.util.ArrayList;

public class CustomWeatherListAdapter extends ArrayAdapter<Weatherw> implements View.OnClickListener{

    private ArrayList<Weatherw> dataSet;
    Context mContext;


    // View lookup cache
    private static class ViewHolder {
        TextView temp;
        TextView time;
        ImageView info;
        ImageView weatherIcon;
    }



    public CustomWeatherListAdapter(ArrayList<Weatherw> data, Context context) {
        super(context, R.layout.row_item1, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        Weatherw model = (Weatherw) object;

        switch (v.getId())
        {
            case R.id.item_info:
                assert model != null;
                Snackbar.make(v, " " +model.getPhrase(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Weatherw model = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {


            assert model != null;

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item1, parent, false);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);
            viewHolder.temp = (TextView) convertView.findViewById(R.id.date);
            viewHolder.info = (ImageView) convertView.findViewById(R.id.item_info);
            viewHolder.weatherIcon = (ImageView) convertView.findViewById(R.id.weatherIcon);


            result=convertView;

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        assert model != null;

        viewHolder.temp.setText(" " + model.getTemp() + "°C");
        viewHolder.time.setText(" " + model.getTime());
        viewHolder.weatherIcon.setImageResource(model.getIcon());

        viewHolder.info.setOnClickListener(this);
        viewHolder.info.setTag(position);



        // Return the completed view to render on screen
        return convertView;
    }

}