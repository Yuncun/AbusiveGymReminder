package com.pipit.agc.agc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.pipit.agc.agc.fragment.IntroFragment;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.fragment.IntroGoalsFragment;
import com.pipit.agc.agc.fragment.IntroPlacePickerFragment;

/**
 * Created by Eric on 12/12/2015.
 */
public class IntroductionActivity extends FragmentActivity {
    private int backButtonCount = 0;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduction_layout);
        selectFrag(0);
    }


    @Override
    public void onBackPressed()
    {
        if(backButtonCount >= 0)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button again to exit app.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    public void selectFrag(int step) {
        Fragment fr = null;

        switch (step){
            case 0:
                fr = new IntroFragment();
                break;
            case 1:
                fr = new IntroPlacePickerFragment();
                break;
            case 2:
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
