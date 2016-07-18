package com.pipit.agc.agc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.adapter.SettingsAdapter;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] frags = {"Logs", "Test Message DB", "Preferences"};
        ArrayAdapter<String> adapter = new SettingsAdapter(this, frags);
        ListView lv = (ListView)findViewById(R.id.mylist);
        lv.setAdapter(adapter);
        final Intent i = new Intent(SettingsActivity.this, IndividualSettingActivity.class);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                switch (position) {
                    case (0):
                        i.putExtra("fragment", "TestDBFragmentMessages");
                        startActivity(i);
                        break;
                    case (1):
                        i.putExtra("fragment", "PreferencesFragment");
                        startActivity(i);
                        break;
                    default:
                        break;
                }
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
