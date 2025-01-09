package com.chdman;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.chdman.fragments.PreferenceFragment;

public class SettingsActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar tlb = findViewById(R.id.toolbar);
        setSupportActionBar(tlb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_content, new PreferenceFragment())
                .commit();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        } 
        return super.onOptionsItemSelected(item);
    }
}
