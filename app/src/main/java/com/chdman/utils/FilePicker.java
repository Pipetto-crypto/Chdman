package com.chdman.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.LinkedList;

public class FilePicker {
    private LinkedList<String> openedFiles = new LinkedList<>();
    private ActivityResultLauncher<Intent> arl;

    public FilePicker(AppCompatActivity act) {
        super();
        this.arl = act.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String documentID = data.getData().getPath();
                    String file = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+documentID.split(":")[1];
                    openedFiles.add(file);
                }
                else
                    Toast.makeText(act, "No valid file has been selected", Toast.LENGTH_LONG);
            }
        });
    }

    public FilePicker(Fragment frag) {
        super();
        this.arl = frag.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String documentID = data.getData().getPath();
                    String file = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+documentID.split(":")[1];
                    openedFiles.add(file);
                }
                else
                    Toast.makeText(frag.getActivity(), "No valid file has been selected", Toast.LENGTH_LONG);
            }
        });
    }

    public void launch(String mimetype) {
        Intent launcher = new Intent(Intent.ACTION_GET_CONTENT);
        launcher.addCategory(Intent.CATEGORY_OPENABLE);
        launcher.setType(mimetype);
        Intent intent = Intent.createChooser(launcher, "Choose a file");
        this.arl.launch(intent);

    }

    public String getFile() {
        String file = "";
        if (openedFiles.size() > 0)
            file = this.openedFiles.remove();
        return file;
    }
}
