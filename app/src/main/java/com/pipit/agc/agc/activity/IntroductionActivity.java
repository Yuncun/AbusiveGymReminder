package com.pipit.agc.agc.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.pipit.agc.agc.fragment.IntroFragment;
import com.pipit.agc.agc.R;

/**
 * Created by Eric on 12/12/2015.
 */
public class IntroductionActivity extends FragmentActivity {

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduction_layout);
        selectFrag();
    }

    public void selectFrag() {
        Fragment fr;
        fr = new IntroFragment();

        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_page, fr);
        fragmentTransaction.commit();
    }
}
