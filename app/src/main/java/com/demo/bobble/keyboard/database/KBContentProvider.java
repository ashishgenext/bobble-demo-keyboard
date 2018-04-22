package com.demo.bobble.keyboard.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.demo.bobble.keyboard.R;
import com.demo.bobble.keyboard.utility.CommonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class KBContentProvider extends ContentProvider {

    private static SQLiteOpenHelper mOpenHelper;

    private static HashMap<String, String> englishMasterProjectionMap;
    private static HashMap<String, String> englishSuggestProjectionMap;

    private static final int ENGLISH_MASTER = 1;
    private static final int ENGLISH_MASTER_ID = 2;
    private static final int ENGLISH_SUGGEST = 3;
    private static final int ENGLISH_SUGGEST_ID = 4;
    private static final int CUSTOMQUERY = 5;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(DataProvider.AUTHORITY, "englishmaster",
                ENGLISH_MASTER);
        sUriMatcher.addURI(DataProvider.AUTHORITY, "englishmaster/#",
                ENGLISH_MASTER_ID);
        sUriMatcher.addURI(DataProvider.AUTHORITY, "englishsuggest",
                ENGLISH_SUGGEST);
        sUriMatcher.addURI(DataProvider.AUTHORITY, "englishsuggest/#",
                ENGLISH_SUGGEST_ID);


        englishMasterProjectionMap = new HashMap<String, String>();
        englishMasterProjectionMap.put(DataProvider.EnglishMaster.ID,
                DataProvider.EnglishMaster.ID);
        englishMasterProjectionMap.put(DataProvider.EnglishMaster.KEYWORD,
                DataProvider.EnglishMaster.KEYWORD);
        englishMasterProjectionMap.put(DataProvider.EnglishMaster.COUNT,
                DataProvider.EnglishMaster.COUNT);
        englishMasterProjectionMap.put(DataProvider.EnglishMaster.OTHER,
                DataProvider.EnglishMaster.OTHER);

        englishSuggestProjectionMap = new HashMap<String, String>();
        englishSuggestProjectionMap.put(DataProvider.EnglishSuggest.ID,
                DataProvider.EnglishSuggest.ID);
        englishSuggestProjectionMap.put(DataProvider.EnglishSuggest.KEYWORD,
                DataProvider.EnglishSuggest.KEYWORD);
        englishSuggestProjectionMap.put(DataProvider.EnglishSuggest.COUNT,
                DataProvider.EnglishSuggest.COUNT);
        englishSuggestProjectionMap.put(DataProvider.EnglishSuggest.OTHER,
                DataProvider.EnglishSuggest.OTHER);
    }

    private static class DBHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;
        private static final String TAG = "KBContentProvider";
        private static final String DATABASE_PATH = Environment.getExternalStorageDirectory().getPath()+"/demoBobble/";
        private static final String DATABASE_NAME = "demobobble.db";

        public static final String KB_ENGLISH_MASTER_TABLE_NAME = "english_master";
        public static final String KB_ENGLISH_SUGGEST_TABLE_NAME = "english_suggest";
        Context mHelperContext;
        public static Context applicationContext;
        private SQLiteDatabase db ;

        DBHelper(Context argContext) {
            super(argContext, DATABASE_PATH + DATABASE_NAME, null,
                    DATABASE_VERSION);
            mHelperContext = argContext;
            applicationContext = mHelperContext;

        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.w(TAG, "C R E A T I N G   D A T A B A S E   ");

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
            File file = new File(CommonUtils.folderPath  + CommonUtils.TEXT_FILE_NAME);
            if (file.exists()) {
                InputStream inputStream = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputStream));

                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] strings = TextUtils.split(line, " ");
                        addWord(strings[0], Long.parseLong(strings[1]));
                    }
                } finally {
                    reader.close();
                    SharedPreferences pref = applicationContext.getSharedPreferences("shared_pref", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("text_file", true);
                    editor.apply();
                }
                Log.d(TAG, "DONE loading words.");
            }
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



    @Override
    public boolean onCreate() {
        mOpenHelper = new DBHelper(getContext());
        return true;
    }

    public void init(){
        if(mOpenHelper != null) {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            String groupBy = null;
            String having = null;

            switch (sUriMatcher.match(uri)) {
                case CUSTOMQUERY:
                    return mOpenHelper.getReadableDatabase().rawQuery(selection,
                            null);
                case ENGLISH_MASTER:
                    qb.setTables(DBHelper.KB_ENGLISH_MASTER_TABLE_NAME);
                    if (selection == null) {
                        qb.setProjectionMap(englishMasterProjectionMap);
                    }
                    break;
                case ENGLISH_MASTER_ID:
                    qb.setTables(DBHelper.KB_ENGLISH_MASTER_TABLE_NAME);
                    selection = DataProvider.EnglishMaster.ID + "="
                            + uri.getPathSegments().get(1);
                    break;
                case ENGLISH_SUGGEST:
                    qb.setTables(DBHelper.KB_ENGLISH_SUGGEST_TABLE_NAME);
                    if (projection == null) {
                        qb.setProjectionMap(englishSuggestProjectionMap);
                    }
                    break;
                case ENGLISH_SUGGEST_ID:
                    selection = DataProvider.EnglishSuggest.ID + "="
                            + uri.getPathSegments().get(1);
                    qb.setTables(DBHelper.KB_ENGLISH_MASTER_TABLE_NAME);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            Cursor cursor = qb.query(db, projection, selection,
                    selectionArgs, groupBy, having, sortOrder);
            return cursor;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ENGLISH_MASTER:
                return DataProvider.EnglishMaster.CONTENT_TYPE;
            case ENGLISH_MASTER_ID:
                return DataProvider.EnglishMaster.CONTENT_ITEM_TYPE;
            case ENGLISH_SUGGEST:
                return DataProvider.EnglishSuggest.CONTENT_TYPE;
            case ENGLISH_SUGGEST_ID:
                return DataProvider.EnglishSuggest.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri rowUri = null;
        long rowId;
        switch (sUriMatcher.match(uri)) {
            case ENGLISH_MASTER:
                rowId = db
                        .insert(DBHelper.KB_ENGLISH_MASTER_TABLE_NAME, null, values);
                if (rowId > 0) {
                    rowUri = ContentUris.withAppendedId(
                            DataProvider.EnglishMaster.CONTENT_URI, rowId);
                }
                break;
            case ENGLISH_SUGGEST:
                rowId = db
                        .insert(DBHelper.KB_ENGLISH_SUGGEST_TABLE_NAME, null, values);
                if (rowId > 0) {
                    rowUri = ContentUris.withAppendedId(
                            DataProvider.EnglishSuggest.CONTENT_URI, rowId);
                }
                break;

            default:
                throw new SQLException("Failed to insert row into " + uri);
        }
        return rowUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String rowId;
        switch (sUriMatcher.match(uri)) {
            case ENGLISH_MASTER_ID:
                rowId = uri.getPathSegments().get(1);
                selection = DataProvider.EnglishMaster.ID + "=" + rowId;
            case ENGLISH_MASTER:
                count = db
                        .delete(DBHelper.KB_ENGLISH_MASTER_TABLE_NAME, selection, selectionArgs);
                break;
            case ENGLISH_SUGGEST_ID:
                rowId = uri.getPathSegments().get(1);
                selection = DataProvider.EnglishSuggest.ID + "=" + rowId;
            case ENGLISH_SUGGEST:
                count = db
                        .delete(DBHelper.KB_ENGLISH_SUGGEST_TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ENGLISH_MASTER_ID:
                String rowId = uri.getPathSegments().get(1);
                selection = DataProvider.EnglishMaster.ID + "=" + rowId;

            case ENGLISH_MASTER:
                count = db.update(DBHelper.KB_ENGLISH_MASTER_TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case ENGLISH_SUGGEST_ID:
                rowId = uri.getPathSegments().get(1);
                selection = DataProvider.EnglishSuggest.ID + "=" + rowId;
                values.put(DataProvider.EnglishSuggest._ID, rowId);
            case ENGLISH_SUGGEST:
                count = db.update(DBHelper.KB_ENGLISH_SUGGEST_TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return count;
    }
}
