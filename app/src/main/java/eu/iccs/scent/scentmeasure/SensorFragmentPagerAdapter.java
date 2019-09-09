package eu.iccs.scent.scentmeasure;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by theodoropoulos on 27/11/2017.
 */

public class SensorFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[]= new String[] { "Temperature", "Moisture", "Options" };
    private Context context;
    FragmentManager fm;
    public SensorFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.fm=fm;
        this.context = context;
        tabTitles[0]=  context.getString(R.string.temperature);
        tabTitles[1]=  context.getString(R.string.moisture);
        tabTitles[2]=  context.getString(R.string.options);

    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        //Here lauynch fragment according to the measurement type
        if (position==0) {
            TemperatureFragment tempFragment=TemperatureFragment.newInstance(position + 1);
            return tempFragment;
        }
        else if (position==1)
            return MoistureFragment.newInstance(position + 1);
        else
            return OptionsFragment.newInstance(position + 1);

    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}