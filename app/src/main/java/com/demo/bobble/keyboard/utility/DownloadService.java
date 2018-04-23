package com.demo.bobble.keyboard.utility;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlToDownload = intent.getStringExtra("url");
        ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        File root = new File(CommonUtils.folderPath);
        if (!root.exists()) {
            root.mkdirs();
        }
        File downloadFile = new File(root, CommonUtils.TEXT_FILE_NAME);
        try {
            downloadFile.createNewFile();
            URL downloadURL = new URL(urlToDownload);
            HttpURLConnection conn = (HttpURLConnection) downloadURL
                    .openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200)
                throw new Exception("Error in connection");
            InputStream is = conn.getInputStream();
            FileOutputStream os = new FileOutputStream(downloadFile);
            byte buffer[] = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                os.write(buffer, 0, byteCount);
            }
            os.close();
            is.close();
            Bundle resultData = new Bundle();
            resultData.putInt("progress", 100);
            receiver.send(UPDATE_PROGRESS, resultData);
        } catch (Exception e) {
            Bundle resultData = new Bundle();
            resultData.putInt("progress", -1);
            receiver.send(UPDATE_PROGRESS, resultData);
            e.printStackTrace();
        }
    }
}