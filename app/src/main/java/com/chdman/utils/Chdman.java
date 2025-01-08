package com.chdman.utils;

import android.app.ActionBar;
import android.content.DialogInterface;

import android.os.Environment;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.chdman.R;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Paths;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chdman {
    
    private enum Status {
        INITIALIZED,
        REINITIALIZED,
        RUNNING,
        COMPLETED,
    };

    private Handler handler;
    private String mode;
    private Status status;
    private LinkedList<BufferedReader> outputBufferStack;
    private LinkedList<File> fileStack;
    private LinkedList<Thread> threadStack;
    private Context mContext;
    
    public Chdman(Context ctx) {
        this.mContext = ctx;
        this.mode = "";
        this.handler = new Handler(Looper.getMainLooper());
        this.fileStack = new LinkedList<>();
        this.outputBufferStack = new LinkedList<>();
        this.threadStack = new LinkedList<>();
        this.status = Status.INITIALIZED;
    }
    
    private void clean() {
        fileStack.clear();
        outputBufferStack.clear();
        threadStack.clear();
        mode = "";
        status = Status.REINITIALIZED;
    }
    
    private AlertDialog createProgressBarDialog(String title) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        AlertDialog dialog;
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_bar);
        dialog = builder.create();
        return dialog;
    }
    
    private AlertDialog createModesDialog(String title) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        AlertDialog dialog;
        builder.setTitle(title);
        builder.setCancelable(false);
        String[] modes = {"createcd", "createdvd"};
        final ArrayAdapter<String> adp = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, modes);
        final Spinner sp = new Spinner(mContext);
        sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sp.setAdapter(adp);
        builder.setView(sp);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mode = sp.getSelectedItem().toString();
            }
        });
        dialog = builder.create();
        return dialog;
    }
    
    private void showAlertDialog(AlertDialog dlg) {
        final Runnable showDialog = new Runnable() {
            @Override
            public void run() {
                dlg.show();
            }
        };
        handler.post(showDialog);
    }
    
    private void hideAlertDialog(AlertDialog dlg) {
        final Runnable hideDialog = new Runnable() {
            @Override
            public void run() {
                dlg.hide();
            }
        };
        handler.post(hideDialog);
    }
    
    public void startCompression() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        AlertDialog progressDialog = createProgressBarDialog("Compress");
        progressDialog.setMessage("");
        AlertDialog modesDialog = createModesDialog("Select mode");
        if (!threadStack.isEmpty())
            showAlertDialog(modesDialog);
        executor.execute(new Runnable(){
            @Override
            public void run() {
                Thread processThread = null;
                File outputFile = null;
                if (threadStack.isEmpty())
                    status = Status.COMPLETED;
                if (status == Status.COMPLETED) {
                    clean();
                    hideAlertDialog(progressDialog);
                    return;    
                }
                if (mode != "" && status != Status.RUNNING) {
                    status = Status.RUNNING;
                    showAlertDialog(progressDialog);
                }    
                if (status == Status.RUNNING) {
                    try {
                        processThread = threadStack.pop();
                        outputFile = fileStack.pop();
                        String fileName = outputFile.getName();
                        progressDialog.setMessage(fileName);     
                        processThread.start();
                        processThread.join();
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }          
                executor.execute(this);
            } 
        });
    }
    
    public void addToCompressionQueue(String file) {
        String output;
        if (!file.contains(Environment.getExternalStorageDirectory().getPath())) 
            output = mContext.getExternalFilesDir("") + "/" + file.substring(file.lastIndexOf("/"), file.lastIndexOf(".")) + ".chd";
        else 
            output = file.substring(0, file.lastIndexOf(".")) + ".chd";
        File outputFile = new File(output);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                switch(mode) {
                    case "createcd":
                        createcd(file, output);
                        break;
                    case "createdvd":
                        createdvd(file, output);
                        break;
                }
            }
        };
        if (!outputFile.exists()) {
            fileStack.add(outputFile);
            Thread cmdThread = new Thread(r);
            threadStack.add(cmdThread);
        }    
    }

    private native void createcd(String in, String out);
    private native void createdvd(String in, String out);

    static {
        System.loadLibrary("chdman");
    }
}
