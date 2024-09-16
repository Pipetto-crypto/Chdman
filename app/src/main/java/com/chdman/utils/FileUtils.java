package com.chdman.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private Context mContext;

    public FileUtils(Context context) {
        super();
        this.mContext = context;
    }

    public void copyAsset(String name, String dest) {
        AssetManager am = mContext.getAssets();
        try {
            File destFile = new File(dest + "/" + name);
            InputStream in = am.open(name);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile));
            byte[] buffer = new byte[1024];
            int lenghtRead;
            while ((lenghtRead = in.read(buffer)) > 0){
                out.write(buffer, 0, lenghtRead);
                out.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
