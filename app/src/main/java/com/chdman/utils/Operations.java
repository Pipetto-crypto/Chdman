package com.chdman.utils;
import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;

public final class Operations {
    
    public static String pendingOperation = "";
    public Operations() {}
    
   public static void compress(AppCompatActivity mActivity){
        UriParser uriParser = new UriParser(mActivity);
        Chdman chdman = new Chdman(mActivity);
        ArrayList<Uri> filesList = FilePicker.getFiles();
        ArrayList<String> list = new ArrayList<String>();
        for (Uri uri : filesList) {
            if (uri.toString().contains("tree"))
                list.addAll(uriParser.fromTreeUriToFilePath(uri));
            else {
                String filePath = uriParser.fromUriToFilePath(uri);
                list.add(filePath);
            }    
        }
        for (String file : list) {
            String extension = file.substring(file.lastIndexOf(".") + 1, file.length());
            if (extension.equals("iso") || extension.equals("cue") || extension.equals("gdi")) {
                chdman.addToCompressionQueue(file);
            }     
        }
        chdman.startCompression();
        filesList.clear();
        pendingOperation = "";
    }
    
    public static void transfer(AppCompatActivity mActivity) {
        ArrayList<Uri> filesList = FilePicker.getFiles();
        DocumentFile destinationDir = DocumentFile.fromTreeUri(mActivity, filesList.get(0));
        File sourceDir = new File(mActivity.getExternalFilesDir("").getPath());
        for (File f : sourceDir.listFiles()) {
            try {
                String sourceName = f.getName();
                Uri destinationFileUri = DocumentsContract.createDocument(mActivity.getContentResolver(), destinationDir.getUri(), "application/octet-stream", sourceName);
                InputStream is = new FileInputStream(f);
                OutputStream os = mActivity.getContentResolver().openOutputStream(destinationFileUri);
                if (is != null && os != null) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    is.close();
                    os.close();
                    f.delete();
                }    
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        filesList.clear();
        pendingOperation = "";
    }
}
