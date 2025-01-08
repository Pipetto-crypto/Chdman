package com.chdman.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import com.chdman.MainActivity;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.File;

public class FilePicker {
    private static ArrayList<Uri> openedFiles;
    private Context mContext;
    private ActivityResultLauncher<Intent> arl;

    public FilePicker(AppCompatActivity act) {
        this.mContext = act;
        this.arl = act.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    openedFiles = new ArrayList<>();
                    Intent data = result.getData();
                    int flags = data.getFlags();
                    act.getContentResolver().takePersistableUriPermission(data.getData(), (flags & Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));   
                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            openedFiles.add(data.getClipData().getItemAt(i).getUri());     
                        }
                    }
                    else
                        openedFiles.add(data.getData());
                    switch (Operations.pendingOperation) {
                            case "compress":
                                Operations.compress(act);
                                break;
                            case "transfer":
                                Operations.transfer(act);
                                break;
                    }
                }
            }
        });
    }

    public void pickMultiple(String mimetype) {
        Intent launcher = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        launcher.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        launcher.addCategory(Intent.CATEGORY_OPENABLE);
        launcher.setType(mimetype);
        this.arl.launch(launcher);
    }
    
    public void pickFolder() {
        Intent launcher = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        launcher.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.arl.launch(launcher);
    }

    public static ArrayList<Uri> getFiles() {
        return openedFiles;
    }
}
