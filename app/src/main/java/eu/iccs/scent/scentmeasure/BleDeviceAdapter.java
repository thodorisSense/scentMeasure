package eu.iccs.scent.scentmeasure;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by theodoropoulos on 23/10/2017.
 */

public class BleDeviceAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<BleDevice> mDataSource;

    public BleDeviceAdapter(Context context, ArrayList<BleDevice> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    //2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    //3
    @Override
    public long getItemId(int position) {
        return position;
    }

    //4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for row item
        View rowView = mInflater.inflate(R.layout.list_item_ble_devices, parent, false);
        // Get title element
        TextView titleTextView =
                (TextView) rowView.findViewById(R.id.suggestion_text);

        TextView deviceIdView =
                (TextView) rowView.findViewById(R.id.id_text);

        //ImageButton imageButton= (ImageButton) rowView.findViewById(R.id.RenameDeviceButton);

        /*
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        */
// 1
        BleDevice device = (BleDevice) getItem(position);

// 2
        if (device.bleDeviceId!=null){
            if (!device.bleDeviceId.equals("Flower care")) {
                titleTextView.setText(device.bleDeviceId);
                ImageView imageView = (ImageView) rowView.findViewById(R.id.imageViewLeft);
                imageView.setImageResource(R.mipmap.baseline_error_outline_black_48dp);
            }
        }
        else
        {
            ImageView imageView = (ImageView) rowView.findViewById(R.id.imageViewLeft);
            imageView.setImageResource(R.mipmap.baseline_error_outline_black_48dp);

        }
        if ( device.bleDeviceAlias==null)
            deviceIdView.setText(device.bleDeviceInfo);
        else
            deviceIdView.setText(device.bleDeviceAlias);

        return rowView;
    }



}
