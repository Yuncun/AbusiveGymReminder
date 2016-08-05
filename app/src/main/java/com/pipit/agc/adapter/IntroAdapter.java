package com.pipit.agc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;

import com.pipit.agc.R;
import com.pipit.agc.fragment.IntroViewPagerFragment;

/**
 * Created by Eric on 4/13/2016.
 */
public class IntroAdapter extends FragmentPagerAdapter {
    private Context _context;
    private IntroViewPagerFragment pageTwoFragment;

    public IntroAdapter(FragmentManager fm, Context context) {
        super(fm);
        _context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return IntroViewPagerFragment.newInstance(ContextCompat.getColor(_context, R.color.basewhite), position);
            case 1:
                pageTwoFragment = IntroViewPagerFragment.newInstance(ContextCompat.getColor(_context, R.color.basewhite), position);
                return pageTwoFragment;
            default:
                return IntroViewPagerFragment.newInstance(ContextCompat.getColor(_context, R.color.basewhite), position);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    //Call this when we want to start an animation on a given page
    public void notifyStartAnimation(int page){
        if (pageTwoFragment!=null) pageTwoFragment.startAnimation();
    }

}