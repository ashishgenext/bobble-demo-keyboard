package com.demo.bobble.keyboard.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.demo.bobble.keyboard.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class helps open, create, and upgrade the database file.
 */
class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "KBContentProvider";
    private static final String DATABASE_PATH = Environment.getExternalStorageDirectory().getPath()+"/demoBobble/";
    private static final String DATABASE_NAME = "demoBobble.db";

    public static final String KB_ENGLISH_MASTER_TABLE_NAME = "english_master";
    public static final String KB_ENGLISH_SUGGEST_TABLE_NAME = "english_suggest";
    Context mHelperContext;
    public static Context applicationContext;

    DBHelper(Context argContext) {
        super(argContext, DATABASE_PATH + DATABASE_NAME, null,
                DATABASE_VERSION);
        mHelperContext = argContext;
        applicationContext = mHelperContext;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String table = "CREATE TABLE IF NOT EXISTS " + KB_ENGLISH_MASTER_TABLE_NAME
                + " (";
        table += DataProvider.EnglishMaster.ID
                + "  INTEGER PRIMARY KEY AUTOINCREMENT,";
        table += DataProvider.EnglishMaster.KEYWORD + " varchar,";
        table += DataProvider.EnglishMaster.COUNT + " int,";
        table += DataProvider.EnglishMaster.OTHER + " varchar";
        table += " );";
        sqLiteDatabase.execSQL(table);

        table = "CREATE TABLE IF NOT EXISTS " + KB_ENGLISH_SUGGEST_TABLE_NAME
                + " (";
        table += DataProvider.EnglishSuggest.ID
                + "  INTEGER PRIMARY KEY AUTOINCREMENT,";
        table += DataProvider.EnglishSuggest.KEYWORD + " varchar,";
        table += DataProvider.EnglishSuggest.COUNT + " int,";
        table += DataProvider.EnglishSuggest.OTHER + " varchar";
        table += " );";
        sqLiteDatabase.execSQL(table);

        loadMasterDictionary();
    }

    @Override
    public void onOpen(SQLiteDatabase argDB) {
    }

    // do need to change this method when the user is doing an upgrade.
    @Override
    public void onUpgrade(SQLiteDatabase argDB, int argOldVersion, int argNewVersion) {
        onCreate(argDB);
    }

    /**
     * Starts a thread to load the database table with words
     */
    private void loadMasterDictionary() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    loadWords();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void loadWords() throws IOException {
        Log.d(TAG, "Loading words...");
        final Resources resources = mHelperContext.getResources();
        InputStream inputStream = resources
                .openRawResource(R.raw.demo_word_list);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                 String[] strings = TextUtils.split(line, " ");
                addWord(strings[0],Long.parseLong(strings[1]));
            }
        } finally {
            reader.close();
        }
        Log.d(TAG, "DONE loading words.");
    }

    /**
     * Add a word to the dictionary.
     *
     * @return rowId or -1 if failed
     */
    public void addWord(String word ,long count) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(DataProvider.EnglishMaster.KEYWORD, word);
        initialValues.put(DataProvider.EnglishMaster.COUNT, count);
        Log.d(TAG, "**addWORD**");
        applicationContext.getContentResolver().insert(
                DataProvider.EnglishMaster.CONTENT_URI, initialValues);
    }
}
