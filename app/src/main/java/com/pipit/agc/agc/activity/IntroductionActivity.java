package com.pipit.agc.agc.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.pipit.agc.agc.fragment.IntroFragment;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.fragment.IntroGoalsFragment;

/**
 * Created by Eric on 12/12/2015.
 */
public class IntroductionActivity extends FragmentActivity {

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduction_layout);
        selectFrag(0);
    }

    public void selectFrag(int step) {
        Fragment fr = null;

        switch (step){
            case 0:
                fr = new IntroFragment();
                break;
            case 1:
                fr = IntroGoalsFragment.newInstance();
                break;
            default:
                //nothing
        }

        if (fr == null) return;
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_page, fr);
        fragmentTransaction.commit();
    }
}
