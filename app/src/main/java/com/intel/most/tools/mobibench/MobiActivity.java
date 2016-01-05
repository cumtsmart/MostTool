package com.intel.most.tools.mobibench;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.astuetz.PagerSlidingTabStrip;
import com.intel.most.tools.R;
import com.intel.most.tools.mobibench.fragment.HistoryFragment;
import com.intel.most.tools.mobibench.fragment.MeasureFragment;
import com.intel.most.tools.mobibench.fragment.SettingFragment;

public class MobiActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private PagerTitleStrip pagerTitleStrip;
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
        // pagerTitleStrip = (PagerTitleStrip)findViewById(R.id.pager_title_strip);


        myPagerAdapter = new MyPagerAdapter(getFragmentManager());
        viewPager.setAdapter(myPagerAdapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(viewPager);

        measureFragment = new MeasureFragment();
        historyFragment = new HistoryFragment();
        settingFragment = new SettingFragment();
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
