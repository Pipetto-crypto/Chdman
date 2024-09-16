package com.chdman;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.chdman.utils.FileUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public MainActivity getInstance() {
        return this;
    }

    public void extractChdmanBinary() {
        FileUtils futils = new FileUtils(getInstance());
        File binaryDir = new File(getFilesDir().getAbsolutePath() + "/bin/");
        File binary = new File(binaryDir.getAbsolutePath() + "/chdman");
        if (!binaryDir.exists())
            binaryDir.mkdirs();
        if (!binary.exists()) {
            futils.copyAsset("chdman", binaryDir.getAbsolutePath());
            binary.setExecutable(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        extractChdmanBinary();
    }
}
