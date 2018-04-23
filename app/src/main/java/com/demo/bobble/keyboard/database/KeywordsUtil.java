package com.demo.bobble.keyboard.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.demo.bobble.keyboard.utility.CommonUtils;

//Database access object

public class KeywordsUtil {

    Context mContext;

    public KeywordsUtil(Context context) {
        this.mContext = context;
    }

    public void insertOrUpdateKeywordsInDB(String szKeyword) {
        if (CommonUtils.findStoragePermission(mContext)) {

            ContentValues values = new ContentValues();
            values.put(DataProvider.EnglishSuggest.KEYWORD, szKeyword);
            values.put(DataProvider.EnglishSuggest.COUNT, 1);

            String selection = DataProvider.EnglishSuggest.KEYWORD + " = ?";
            String[] selectionArgs = new String[]{szKeyword};

            Cursor cursor = mContext.getContentResolver().query(
                    DataProvider.EnglishSuggest.CONTENT_URI, null, selection,
                    selectionArgs, null);

            if (cursor == null || !cursor.moveToFirst()) {// insert fresh value into
                // Roster DB
                mContext.getContentResolver().insert(
                        DataProvider.EnglishSuggest.CONTENT_URI, values);
            } else {
                // if count < X update existing count in Roster DB
                int nCount = cursor.getInt(cursor
                        .getColumnIndex(DataProvider.EnglishSuggest.COUNT));
                if (nCount < CommonUtils.nCountOfValueInRoster) {
                    values.put(DataProvider.EnglishSuggest.COUNT, nCount + 1);
                    mContext.getContentResolver().update(
                            DataProvider.EnglishSuggest.CONTENT_URI, values,
                            selection, selectionArgs);
                } else {// insert into Master DB and delete from Roster
                    mContext.getContentResolver().delete(
                            DataProvider.EnglishSuggest.CONTENT_URI, selection,
                            selectionArgs);
                    if (!bIsKeywordInMasterDB(szKeyword))
                        mContext.getContentResolver().insert(
                                DataProvider.EnglishMaster.CONTENT_URI,
                                values);
                }
            }
        }

    }

    public void insertOrUpdateKeywordsInMasterDb(String szKeyword) {
        if (CommonUtils.findStoragePermission(mContext)) {

            //Insert in master DB

            ContentValues values = new ContentValues();
            values.put(DataProvider.EnglishMaster.KEYWORD, szKeyword);
            values.put(DataProvider.EnglishMaster.COUNT, 1);

            String selection = DataProvider.EnglishSuggest.KEYWORD + " = ?";
            String[] selectionArgs = new String[]{szKeyword};

            Cursor cursor = mContext.getContentResolver().query(
                    DataProvider.EnglishMaster.CONTENT_URI, null, selection,
                    selectionArgs, null);

            if (cursor == null || !cursor.moveToFirst()) {// insert fresh value into
                mContext.getContentResolver().insert(
                        DataProvider.EnglishMaster.CONTENT_URI, values);
            } else {
                int nCount = cursor.getInt(cursor
                        .getColumnIndex(DataProvider.EnglishMaster.COUNT));

                values.put(DataProvider.EnglishMaster.COUNT, nCount + 1);
                mContext.getContentResolver().update(
                        DataProvider.EnglishMaster.CONTENT_URI, values,
                        selection, selectionArgs);
            }

            // Insert in Suggest DB
            values.clear();
            values.put(DataProvider.EnglishSuggest.KEYWORD, szKeyword);
            values.put(DataProvider.EnglishSuggest.COUNT, 1);
            values.put(DataProvider.EnglishSuggest.SYNC_FLAG, 0);
            mContext.getContentResolver().insert(DataProvider.EnglishSuggest.CONTENT_URI, values);


        }
    }


    public Cursor getMatchingWordsFromMasterDB(String szKeyword) {
        if (CommonUtils.findStoragePermission(mContext)) {
            String selection = DataProvider.EnglishSuggest.KEYWORD + " LIKE ?";
            String[] selectionArgs = new String[]{szKeyword + "%"};

            Cursor cursor = mContext.getContentResolver().query(
                    DataProvider.EnglishMaster.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    DataProvider.EnglishMaster.COUNT + " DESC LIMIT "
                            + CommonUtils.nDbSearchLimit);


            if (cursor != null && !cursor.moveToFirst()) {
                cursor.close();
                return null;
            } else
                return cursor;
        }
        return null;
    }

    public Cursor getTopWordsFromSuggestDB() {
        if (CommonUtils.findStoragePermission(mContext)) {
            String[] selectionArgs = null;

            Cursor cursor = mContext.getContentResolver().query(
                    DataProvider.EnglishSuggest.CONTENT_URI,
                    new String[]{"Distinct " + DataProvider.EnglishSuggest.KEYWORD},
                    null,
                    selectionArgs,
                    DataProvider.EnglishMaster._ID + " DESC LIMIT "
                            + CommonUtils.nDbTopSearchLimit);


            if (cursor != null && !cursor.moveToFirst()) {
                cursor.close();
                return null;
            } else
                return cursor;
        }
        return null;
    }

    public Cursor getTopWordsFromDB() {
        if (CommonUtils.findStoragePermission(mContext)) {
            String[] selectionArgs = null;

            Cursor cursor = mContext.getContentResolver().query(
                    DataProvider.EnglishMaster.CONTENT_URI,
                    null,
                    null,
                    selectionArgs,
                    DataProvider.EnglishMaster.COUNT + " DESC LIMIT "
                            + CommonUtils.nDbTopSearchLimit);


            if (cursor != null && !cursor.moveToFirst()) {
                cursor.close();
                return null;
            } else
                return cursor;
        }
        return null;
    }

    public boolean bIsKeywordInMasterDB(String szKeyword) {
        String selection = DataProvider.EnglishMaster.KEYWORD + " = ?";
        String[] selectionArgs = new String[]{szKeyword};

        Cursor cursor = mContext.getContentResolver().query(
                DataProvider.EnglishMaster.CONTENT_URI, null,
                selection, selectionArgs, null);
        if (cursor == null) {
            return false;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        } else
            return true;
    }

    public void updateMasterDB(String szKeyword, int count) {
        if (CommonUtils.findStoragePermission(mContext)) {
            String selection = DataProvider.EnglishMaster.KEYWORD + " = ?";
            String[] selectionArgs = new String[]{szKeyword};

            ContentValues values = new ContentValues();
            values.put(DataProvider.EnglishMaster.COUNT, count + 1);
            mContext.getContentResolver().update(
                    DataProvider.EnglishMaster.CONTENT_URI, values,
                    selection, selectionArgs);
        }
    }

    public Cursor getUnsyncedWords() {
        if (CommonUtils.findStoragePermission(mContext)) {
            String selection = DataProvider.EnglishSuggest.SYNC_FLAG + " = ?";
            String[] selectionArgs = new String[]{"0"};

            Cursor cursor = mContext.getContentResolver().query(
                    DataProvider.EnglishSuggest.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null);


            if (cursor != null && !cursor.moveToFirst()) {
                cursor.close();
                return null;
            } else
                return cursor;
        }
        return null;
    }

    public void updateSyncState(Long id){
        if (CommonUtils.findStoragePermission(mContext)) {
            String selection = DataProvider.EnglishSuggest._ID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(id)};

            ContentValues values = new ContentValues();
            values.put(DataProvider.EnglishSuggest.SYNC_FLAG, 1);
            mContext.getContentResolver().update(
                    DataProvider.EnglishSuggest.CONTENT_URI, values,
                    selection, selectionArgs);
        }
    }

}
