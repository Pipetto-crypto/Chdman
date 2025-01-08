package com.chdman.utils;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UriParser {
    
    private Context mContext;
    
    public UriParser(Context ctx) {
        mContext = ctx;
    }
    
    public String fromUriToFilePath(Uri uri) {
        ParcelFileDescriptor pfd;
        String file;
        try {
            pfd = mContext.getContentResolver().openFileDescriptor(uri, "r");
            int fd = pfd.getFd();
            String linkFilePath = "/proc/self/fd/" + fd;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Path linkPath = Paths.get(linkFilePath);
                file = Files.readSymbolicLink(linkPath).toString();
            }    
            else 
            {
                 File linkFile = new File(linkFilePath);
                 file = linkFile.getCanonicalPath();
            }
            pfd.close();
            file = file.replace("/mnt/user/0", "/storage");
            file = file.replace("/mnt/media_rw", "/storage");
            Log.d("UriParser", String.format("Uri %s has been coverted to %s", uri, file));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
    
    public ArrayList<String> fromTreeUriToFilePath(Uri treeUri) {
        ArrayList<String> filePaths = new ArrayList<String>();
        DocumentFile dir = DocumentFile.fromTreeUri(mContext, treeUri);
        for (DocumentFile file : dir.listFiles()) {
            filePaths.add(fromUriToFilePath(file.getUri()));
        }
        return filePaths;
    }
}
