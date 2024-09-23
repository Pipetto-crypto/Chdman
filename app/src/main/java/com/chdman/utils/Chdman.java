package com.chdman.utils;
import android.content.DialogInterface;
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
    private Thread outputThread;
    private BufferedReader outputBuffer;
    private File outputFile;
    private Process p;
    private Context mContext;
    
    private Runnable printToLogcat() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                     outputBuffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
                     String line;
                     while (!Thread.currentThread().isInterrupted()) {
                         line = outputBuffer.readLine();
                         if (line != null)   
                            Log.d("CHDMAN: ", line);
                     }
                 }       
                 catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }     
        };
        return r;
    }
    
    private AlertDialog createAlertDialog(String title, String message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        AlertDialog dialog;
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int btn) {
                outputThread.interrupt(); 
                try {
                    outputBuffer.close();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }     
                if (p.isAlive())
                    p.destroyForcibly();
                if (outputFile.exists())    
                    outputFile.delete();
                dialog.dismiss();
            }
        });
        builder.setView(R.layout.progress_bar);
        dialog = builder.create();
        return dialog;
    }
    
    private void showAlertDialog(AlertDialog dlg) {
        handler.post (new Runnable(){
             @Override
             public void run() {
                 dlg.show();
             }
        });
    }
    
    private void hideAlertDialog(AlertDialog dlg) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dlg.hide();
            }
        };
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!p.isAlive()) 
                    handler.post(runnable);
            }        
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    public Chdman(Context ctx) {
        this.mContext = ctx;
        this.executable = mContext.getFilesDir().getAbsolutePath() + "/bin/chdman";
        this.handler = new Handler(Looper.getMainLooper());
        this.p = null;
        this.outputThread = null;
    }
    
    public void compress(String file) {
        outputThread = new Thread(printToLogcat());
        String fileName = Paths.get(file).getFileName().toString();
        String output = file.substring(0, file.lastIndexOf(".")) + ".chd";
        outputFile = new File(output);
        String[] cmd = {this.executable, "createcd", "-i", file, "-o", output};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        AlertDialog dialog = createAlertDialog("Compress", fileName);
        showAlertDialog(dialog);
        try {
            p = pb.start();
            outputThread.start();
            hideAlertDialog(dialog);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
