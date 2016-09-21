package com.pipit.agc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pipit.agc.adapter.SettingsAdapter;
import com.pipit.agc.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] frags = {"Logs", "Test Message DB"};
        ArrayAdapter<String> adapter = new SettingsAdapter(this, frags);
        ListView lv = (ListView)findViewById(R.id.mylist);
        lv.setAdapter(adapter);
        final Intent i = new Intent(SettingsActivity.this, IndividualSettingActivity.class);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                switch (position) {
                    case (0):
                        i.putExtra("fragment", "LogFragment");
                        startActivity(i);
                        break;
                    case (1):
                        i.putExtra("fragment", "DevTestingFragment");
                        startActivity(i);
                        break;

                    default:
                        break;
                }
            }
        });
    }

}
