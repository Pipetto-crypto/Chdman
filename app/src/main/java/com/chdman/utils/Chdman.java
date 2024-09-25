package com.chdman.utils;
import android.content.DialogInterface;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.chdman.R;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

public class Chdman {
    
    private String executable;
    private Handler handler;
    private boolean interrupter;
    private int completedOperations;
    private LinkedList<BufferedReader> outputBufferQueue;
    private ArrayList<File> outputFileList;
    private LinkedList<Thread> threadQueue;
    private Context mContext;
    
    private void cancel() {
        interrupter = true;
        for (Thread t : threadQueue) {
            t.interrupt();
        }
        try {
            for (BufferedReader br : outputBufferQueue) {
                br.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (File f : outputFileList)
            f.delete();    
    }
    
    private void reset() {
        outputFileList.clear();
        outputBufferQueue.clear();
        threadQueue.clear();
        interrupter = false;
        completedOperations = 0;
    }
    
    private AlertDialog createAlertDialog(String title) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        AlertDialog dialog;
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int btn) {
                cancel();
            }
        });
        builder.setView(R.layout.progress_bar);
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
    
    private void processProgressBar(AlertDialog dlg) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        dlg.setMessage(outputFileList.get(completedOperations).getName());
        showAlertDialog(dlg);
        executor.execute(new Runnable(){
            @Override
            public void run() {
                if (threadQueue.isEmpty() || interrupter) {
                    hideAlertDialog(dlg);
                }           
                else {
                    try {
                        Thread processThread = threadQueue.peek();
                        processThread.start();
                        processThread.join();
                        if (!interrupter) {
                            threadQueue.pop();     
                            completedOperations++;
                            String fileName = outputFileList.get(completedOperations).getName();
                            dlg.setMessage(fileName);     
                        }
                        executor.execute(this);    
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }   
                }     
            } 
        });
    }
    
    public Chdman(Context ctx) {
        this.mContext = ctx;
        this.executable = mContext.getFilesDir().getAbsolutePath() + "/bin/chdman";
        this.handler = new Handler(Looper.getMainLooper());
        this.outputFileList = new ArrayList<>();
        this.outputBufferQueue = new LinkedList<>();
        this.threadQueue = new LinkedList<>();
        this.completedOperations = 0;
        this.interrupter = false;
    }
    
    private void createcd(String file) {
        String output = file.substring(0, file.lastIndexOf(".")) + ".chd";
        File outputFile = new File(output);
        outputFileList.add(outputFile);
        String[] cmd = {this.executable, "createcd", "-i", file, "-o", output};
        final ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                     Process p = pb.start();
                     BufferedReader outputBuffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
                     outputBufferQueue.add(outputBuffer);
                     String line;
                     while (!Thread.currentThread().isInterrupted()) {
                         line = outputBuffer.readLine();
                         if (line != null)   
                            Log.d("CHDMAN: ", line);
                         else {
                            Thread.currentThread().interrupt();
                         }   
                     }
                 }       
                 catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }     
        };
        Thread cmdThread = new Thread(r);
        threadQueue.add(cmdThread);
    }
    
    public void batchCompress(LinkedList<String> list) {
        reset();
        AlertDialog dialog = createAlertDialog("Compress");
        for (String file : list) {
            String extension = file.substring(file.lastIndexOf(".") + 1, file.length());
            if (extension.equals("iso") || extension.equals("cue") || extension.equals("gdi")) {
                createcd(file);
            } 
        }
        if (threadQueue.size() > 0) {
            processProgressBar(dialog);
        }
        list.clear();    
    }
}
