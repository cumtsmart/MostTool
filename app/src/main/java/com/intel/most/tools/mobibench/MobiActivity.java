package com.intel.most.tools.mobibench;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.astuetz.PagerSlidingTabStrip;
import com.intel.most.tools.R;
import com.intel.most.tools.mobibench.fragment.HistoryFragment;
import com.intel.most.tools.mobibench.fragment.MeasureFragment;
import com.intel.most.tools.mobibench.fragment.SettingFragment;

import esos.MobiBench.MobiBenchExe;

public class MobiActivity extends Activity {

    private ViewPager viewPager;
    private MyPagerAdapter myPagerAdapter;

    // 3 fragment
    private MeasureFragment measureFragment;
    private HistoryFragment historyFragment;
    private SettingFragment settingFragment;

    private String[] titles = {"Measure", "History", "Setting"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobi);

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        myPagerAdapter = new MyPagerAdapter(getFragmentManager());
        viewPager.setAdapter(myPagerAdapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(viewPager);

        measureFragment = new MeasureFragment();
        historyFragment = new HistoryFragment();
        settingFragment = new SettingFragment();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String frValue = sharedPref.getString(SettingFragment.KEY_PARTITION, "");
        if (frValue.equals("")) {
            calculateFreeSpace("/data");
        } else {
            calculateFreeSpace(frValue);
        }
    }

    private void calculateFreeSpace(String path) {
        String target_path = null;
        if (path.equals("/data")) {
            target_path = Environment.getDataDirectory().getPath();
        } else if (path.equals("/sdcard")) {
            target_path = getExternalFilesDirs(null)[0].getAbsolutePath();
        } else if (path.equals("/extSdCard")) {
            target_path = MobiBenchExe.sdcard_2nd_path;
        }
        SettingFragment.freeSpace = StorageOptions.getAvailableSize(target_path);
    }

    class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return measureFragment;
                case 1:
                    return historyFragment;
                case 2:
                    return settingFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

}
