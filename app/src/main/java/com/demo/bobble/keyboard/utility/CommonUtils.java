package com.demo.bobble.keyboard.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

public class CommonUtils {

	public static final int nCountOfValueInRoster = 1;
	public static final int nDbSearchLimit = 5;
	public static final int nDbTopSearchLimit = 5;
	private static final int REQUEST_CODE = 1;
	public static final String folderPath = Environment.getExternalStorageDirectory().getPath()+"/demoBobble/";
	public static final String TEXT_FILE_NAME = "word_text_list.txt";
	public static String WORD_LIST_URL = "https://raw.githubusercontent.com/kunaldawn/test_words/master/10000_words.txt";

	public static boolean getStoragePermission(Context context){
		if (Build.VERSION.SDK_INT >= 23) {
			if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
				return false;
			}else {
				return true;
			}
		}
		return true;
	}

	public static boolean findStoragePermission(Context context) {
		if (Build.VERSION.SDK_INT >= 23) {
			if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	}


}
