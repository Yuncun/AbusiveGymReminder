package com.pipit.agc.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.content.Intent;

import com.pipit.agc.util.Constants;
import com.pipit.agc.fragment.MessageBodyFragment;
import com.pipit.agc.R;

/**
 * Displays a given message
 * Created by Eric on 1/25/2016.
 */
public class MessageBodyActivity extends FragmentActivity {
    MessageBodyFragment mbf;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_body);
        Intent intent = getIntent();
        long id = intent.getLongExtra(Constants.MESSAGE_ID, -1);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
             mbf = MessageBodyFragment.newInstance(Long.toString(id));
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mbf).commit();
        }
    }
}
