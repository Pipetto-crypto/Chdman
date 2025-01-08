package com.chdman;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.core.provider.DocumentsContractCompat;
import androidx.documentfile.provider.DocumentFile;
import com.chdman.utils.Chdman;
import com.chdman.utils.FilePicker;

import com.chdman.utils.Operations;
import com.chdman.utils.UriParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.crypto.NullCipher;

public class MainActivity extends AppCompatActivity {
    
    private FilePicker picker;
    private static MainActivity instance;
    private ActivityResultLauncher<Intent> selectItems;
    private ActivityResultLauncher<Intent> selectFolder;
    private UriParser uriParser;
    private ArrayList<Uri> filesToMove;
    private PopupMenu.OnMenuItemClickListener menuListener;
    private View.OnClickListener clickListener;
    private Chdman chdman;
    
    public MainActivity() {
        instance = this;
    }

    public static MainActivity getInstance() {
        return instance;
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
        setContentView(R.layout.activity_main);
        enableStoragePermission();
        menuListener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.compress) {
                    Operations.pendingOperation = "compress";
                    picker.pickMultiple("*/*");
                }     
                else if (item.getItemId() == R.id.batch_compress) {
                    Operations.pendingOperation = "compress";
                    picker.pickFolder();
                }    
                else if (item.getItemId() == R.id.transfer) {
                    Operations.pendingOperation = "transfer";
                    picker.pickFolder();
                }
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
        super.onResume();
    }
}
