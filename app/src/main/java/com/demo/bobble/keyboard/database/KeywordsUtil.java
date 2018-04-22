package com.demo.bobble.keyboard.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.demo.bobble.keyboard.utility.CommonUtils;


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

    void updateMasterDB(String szKeyword, int count) {
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

}
