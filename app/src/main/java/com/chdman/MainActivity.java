package com.chdman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.chdman.utils.Chdman;
import com.chdman.utils.FilePicker;
import com.chdman.utils.FileUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    
    private FilePicker picker;
    private Chdman chdman;

    public MainActivity getInstance() {
        return this;
    }

    private void extractChdmanBinary() {
        FileUtils futils = new FileUtils(getInstance());
        File binaryDir = new File(getFilesDir().getAbsolutePath() + "/bin/");
        File binary = new File(binaryDir.getAbsolutePath() + "/chdman");
        if (!binaryDir.exists())
            binaryDir.mkdirs();
        if (!binary.exists()) {
            futils.copyAsset("chdman", binaryDir.getAbsolutePath());
            binary.setExecutable(true);
        }
    }
    
    private void enableStoragePermission() {
        if(!Environment.isExternalStorageManager()) {
            Intent grantPermission = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(grantPermission);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picker = new FilePicker(getInstance());
        chdman = new Chdman(getInstance());
        setContentView(R.layout.activity_main);
        enableStoragePermission();
        extractChdmanBinary();
        FloatingActionButton btn = findViewById(R.id.fab);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                picker.launch("*/*");
            }          
        });
    }
    
    @Override
    public void onResume() {
        String file = picker.getFile();
        if (file != "") {
            chdman.compress(file);  
        }
        super.onResume();
    }
}
