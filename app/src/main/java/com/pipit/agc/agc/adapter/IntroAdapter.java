package com.pipit.agc.agc.adapter;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.fragment.IntroFragment;
import com.pipit.agc.agc.fragment.IntroViewPagerFragment;

/**
 * Created by Eric on 4/13/2016.
 */
public class IntroAdapter extends FragmentPagerAdapter {

    public IntroAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return IntroViewPagerFragment.newInstance(Color.parseColor("#FFDB8A"), position);
            default:
                return IntroViewPagerFragment.newInstance(Color.parseColor("#71BACC"), position);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

}