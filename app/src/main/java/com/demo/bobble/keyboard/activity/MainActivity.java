package com.demo.bobble.keyboard.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.demo.bobble.keyboard.R;
import com.demo.bobble.keyboard.database.KBContentProvider;
import com.demo.bobble.keyboard.softkeyboard.SoftKeyboard;
import com.demo.bobble.keyboard.utility.CommonUtils;
import com.demo.bobble.keyboard.utility.DownloadService;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    SoftKeyboard mCustomKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        SharedPreferences pref = getApplicationContext().getSharedPreferences("shared_pref", 0);
        boolean isDataInserted = pref.getBoolean("text_file",false);


        if(CommonUtils.getStoragePermission(this) && !isDataInserted){
           // downloadWordTextFile();
            File file = new File(CommonUtils.folderPath + CommonUtils.TEXT_FILE_NAME);
            if (!file.exists()) {
                downloadWordTextFile();
            }else {
                KBContentProvider contentProvider = new KBContentProvider();
                contentProvider.init();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults[1]== PackageManager.PERMISSION_GRANTED){
            downloadWordTextFile();
        }
    }

    private void downloadWordTextFile(){
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("url", CommonUtils.WORD_LIST_URL);
        intent.putExtra("receiver", new DownloadReceiver(new Handler()));
        startService(intent);
    }

    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_PROGRESS) {
                int progress = resultData.getInt("progress");
                if (progress == 100) {
                    KBContentProvider contentProvider = new KBContentProvider();
                    contentProvider.init();
                }else {
                    Toast.makeText(MainActivity.this,"Word Download Failed",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}
