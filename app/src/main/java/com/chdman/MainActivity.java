package com.chdman;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.appcompat.widget.PopupMenu;
import com.chdman.utils.Chdman;
import com.chdman.utils.FilePicker;
import com.chdman.utils.FileUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    
    private FilePicker picker;
    private PopupMenu.OnMenuItemClickListener menuListener;
    private View.OnClickListener clickListener;
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
        final String[] permissions = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        getInstance().requestPermissions(permissions, PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picker = new FilePicker(getInstance());
        MenuItem.OnMenuItemClickListener listener;
        chdman = new Chdman(getInstance());
        setContentView(R.layout.activity_main);
        enableStoragePermission();
        extractChdmanBinary();
        menuListener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.compress) 
                    picker.pickMultiple("*/*");
                else if (item.getItemId() == R.id.batch_compress)
                    picker.pickFolder();
                return true;
            }     
        };
        clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getInstance(), v);
                popup.setOnMenuItemClickListener(menuListener);  
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                popup.show();  
            }    
        };
        FloatingActionButton btn = findViewById(R.id.fab);
        btn.setOnClickListener(clickListener);
    }
    
    
    @Override
    public void onResume() {
        LinkedList<String> filesList = picker.getFilesList();
        if (filesList.size() > 0) 
            chdman.compress(filesList);
        super.onResume();
    }
}
