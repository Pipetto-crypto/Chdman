package com.chdman.utils;
import android.content.DialogInterface;
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
    
    private enum Status {
        INITIALIZED,
        REINITIALIZED,
        RUNNING,
        COMPLETED,
        CANCELLED
    };
    
    private String executable;
    private Handler handler;
    private Status status;
    private LinkedList<BufferedReader> outputBufferStack;
    private LinkedList<File> fileStack;
    private LinkedList<Thread> threadStack;
    private Context mContext;
    
    public Chdman(Context ctx) {
        this.mContext = ctx;
        this.executable = mContext.getFilesDir().getAbsolutePath() + "/bin/chdman";
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
        status = Status.REINITIALIZED;
    }
    
    private AlertDialog createAlertDialog(String title) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        AlertDialog dialog;
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int btn) {
                status = Status.CANCELLED;
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
        dlg.setMessage("");
        showAlertDialog(dlg);
        executor.execute(new Runnable(){
            @Override
            public void run() {
                Thread processThread = null;
                File outputFile = null;
                if (threadStack.isEmpty())
                    status = Status.COMPLETED; 
                if (status == Status.RUNNING){
                    try {
                        processThread = threadStack.pop();
                        outputFile = fileStack.pop();         
                        String fileName = outputFile.getName();
                        dlg.setMessage(fileName);     
                        processThread.start();
                        processThread.join();
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }          
                if (status == Status.COMPLETED) {
                    clean();
                    hideAlertDialog(dlg);
                    return;    
                }
                if (status == Status.CANCELLED) {
                     if (outputFile != null)    
                         outputFile.delete();    
                     try {
                         for (BufferedReader br : outputBufferStack) {
                             br.close();
                         }
                     }
                     catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                     clean();  
                     hideAlertDialog(dlg); 
                     return;     
                 }
                 executor.execute(this);             
            } 
        });
    }
    
    private void createcd(String file) {
        String output = file.substring(0, file.lastIndexOf(".")) + ".chd";
        File outputFile = new File(output);
        String[] cmd = {this.executable, "createcd", "-i", file, "-o", output};
        final ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Process p = null;
                try {
                     p = pb.start();
                     BufferedReader outputBuffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
                     outputBufferStack.add(outputBuffer);
                     String line;
                     while (!Thread.currentThread().isInterrupted()) {
                         line = outputBuffer.readLine();
                         if (line == null || status == Status.CANCELLED)
                             break;
                         Log.d("CHDMAN: ", line);
                     }
                     if(p.isAlive())
                         p.destroyForcibly();
                 }       
                 catch (IOException e) {
                 }
             }     
        };
        if (!outputFile.exists()) {
            fileStack.add(outputFile);
            Thread cmdThread = new Thread(r);
            threadStack.add(cmdThread);
        }    
    }
    
    public void compress(LinkedList<String> list) {
        AlertDialog dialog = createAlertDialog("Compress");
        for (String file : list) {
            String extension = file.substring(file.lastIndexOf(".") + 1, file.length());
            if (extension.equals("iso") || extension.equals("cue") || extension.equals("gdi")) {
                createcd(file);
            } 
        }
        status = Status.RUNNING;
        processProgressBar(dialog);
        list.clear();    
    }
}
