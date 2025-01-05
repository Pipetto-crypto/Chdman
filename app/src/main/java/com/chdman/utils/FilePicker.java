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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.io.File;

public class FilePicker {
    private LinkedList<String> openedFiles = new LinkedList<>();
    private Context mContext;
    private ActivityResultLauncher<Intent> arl;
    
    private void openFromUri(Uri uri) {
        ParcelFileDescriptor pfd;
        String file = "";
        if (uri.getPath().contains("tree")) {
            DocumentFile tree = DocumentFile.fromTreeUri(mContext, uri);
            for (DocumentFile f : tree.listFiles()) {
                try { 
                    pfd = mContext.getContentResolver().openFileDescriptor(f.getUri(), "r");
                    int fd = pfd.getFd();
                    String linkFilePath = "/proc/self/fd/" + fd;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        Path linkPath = Paths.get(linkFilePath);
                        file = Files.readSymbolicLink(linkPath).toString();
                    }    
                    else {
                        File linkFile = new File(linkFilePath);
                        file = linkFile.getCanonicalPath();
                    }
                    pfd.close();  
                    file = file.replace("/mnt/user/0", "/storage");
                    file = file.replace("/mnt/media_rw", "/storage");
                    Log.d("Content picked", file);
                    openedFiles.add(file);
                 }            
                 catch (IOException e) {
                     throw new RuntimeException(e);
                 }
              }
        }
        else {
            try { 
                pfd = mContext.getContentResolver().openFileDescriptor(uri, "r");
                int fd = pfd.getFd();
                String linkFilePath = "/proc/self/fd/" + fd;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Path linkPath = Paths.get(linkFilePath);
                    file = Files.readSymbolicLink(linkPath).toString();
                }    
                else {
                    File linkFile = new File(linkFilePath);
                    file = linkFile.getCanonicalPath();
                }
                pfd.close();  
                file = file.replace("/mnt/user/0", "/storage");
                file = file.replace("/mnt/media_rw", "/storage");
                Log.d("Content picked", file);
                openedFiles.add(file);
             }            
             catch (IOException e) {
                 throw new RuntimeException(e);
             }
        }
    }

    public FilePicker(AppCompatActivity act) {
        this.mContext = act;
        this.arl = act.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            openFromUri(data.getClipData().getItemAt(i).getUri());
                        }
                    }
                    else {
                        openFromUri(data.getData());
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
        Intent intent = Intent.createChooser(launcher, "Choose a file");
        this.arl.launch(intent);
    }
    
    public void pickFolder() {
        Intent launcher = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        this.arl.launch(launcher);
    }

    public LinkedList<String> getFilesList() {
        return this.openedFiles;
    }
}
