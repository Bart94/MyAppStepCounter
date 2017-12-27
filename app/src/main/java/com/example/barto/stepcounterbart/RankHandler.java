package com.example.barto.stepcounterbart;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;


public class RankHandler extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rank_handler_layout);

        ViewPager viewpager = findViewById(R.id.viewPager);
        viewpager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        viewpager.setPageTransformer(true, new CubeOutTransformer());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewpager, true);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return DailyFragment.newInstance("1");
                case 1:
                    return MonthlyFragment.newInstance("2");
                case 2:
                    return DeltaFragment.newInstance("3");
                default:
                    return DailyFragment.newInstance("0");
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}


