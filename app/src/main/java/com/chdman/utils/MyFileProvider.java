package com.chdman.utils;

import android.R;
import android.app.AuthenticationRequiredException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;

public class MyFileProvider extends DocumentsProvider {
    
    @Override
    public boolean onCreate() {
        return true;
    }
    
    private void includeFile(MatrixCursor cursor, String documentId) {
        String filePath = getContext().getExternalFilesDir("").getPath() + "/" + documentId;
        File file = new File(filePath);

        MatrixCursor.RowBuilder row = cursor.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, documentId);
        if (file.isDirectory()) {
            row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
            row.add(Document.COLUMN_FLAGS, Document.FLAG_DIR_SUPPORTS_CREATE);
            row.add(Document.COLUMN_SIZE, 0);
        } 
        else {
            row.add(Document.COLUMN_MIME_TYPE, "*/*");
            row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_DELETE | Document.FLAG_SUPPORTS_WRITE);
            row.add(Document.COLUMN_SIZE, file.length());
        } 
        row.add(Document.COLUMN_DISPLAY_NAME, file.getName());
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
    }

    private String[] resolveRootProjection(String[] projection) {
        if (projection == null || projection.length == 0) {
            return new String[] {
                Root.COLUMN_ROOT_ID,
                Root.COLUMN_MIME_TYPES,
                Root.COLUMN_FLAGS,
                Root.COLUMN_ICON,
                Root.COLUMN_TITLE,
                Root.COLUMN_SUMMARY,
                Root.COLUMN_DOCUMENT_ID,
                Root.COLUMN_AVAILABLE_BYTES,
            };
        } 
        else {
            return projection;
        }
    }
    
    private String[] resolveDocumentProjection(String[] projection) {
        if (projection == null || projection.length == 0) {
        return new String[] {
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE,
        };
        } 
        else {
            return projection;
        }
    }
    
    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));
        final MatrixCursor.RowBuilder row = result.newRow();
        String rootDir = getContext().getExternalFilesDir("").getPath();
        row.add(Root.COLUMN_ROOT_ID, rootDir);
        row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE | Root.FLAG_SUPPORTS_SEARCH | Root.FLAG_SUPPORTS_IS_CHILD);
        row.add(Root.COLUMN_TITLE, "Chdman");
        row.add(Root.COLUMN_DOCUMENT_ID, "/");
        row.add(Root.COLUMN_MIME_TYPES, "*/*");
        row.add(Root.COLUMN_AVAILABLE_BYTES, Integer.MAX_VALUE);
        return result;
    }
    
    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        String rootDir = getContext().getExternalFilesDir("").getPath();
        MatrixCursor cursor = new MatrixCursor(resolveDocumentProjection(projection));

        String parentDocumentPath = rootDir + "/" + parentDocumentId;
        File dir = new File(parentDocumentPath);
        for (File file : dir.listFiles()) {
            String documentId = parentDocumentId + "/" + file.getName();
            includeFile(cursor, documentId);
        }

        return cursor;
    }
    
    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        MatrixCursor cursor = new MatrixCursor(resolveDocumentProjection(projection));
        includeFile(cursor, documentId);

        return cursor;
    }
    // TODO: We need to remove permissions on deleted Uri files
    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException, AuthenticationRequiredException{
        String rootDir = getContext().getExternalFilesDir("").getPath();
        File file = new File(rootDir + "/" + documentId);
        file.delete();
    }
  
    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        String rootDir = getContext().getExternalFilesDir("").getPath();
        File file = new File(rootDir + "/" + documentId);
        final boolean isWrite = (mode.indexOf('w') != -1);
        if (isWrite) {
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
        } 
        else {
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        }
    }
}
