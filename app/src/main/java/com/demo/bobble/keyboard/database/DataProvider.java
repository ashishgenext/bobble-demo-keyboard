package com.demo.bobble.keyboard.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class DataProvider {

    public static final String AUTHORITY = KBContentProvider.class.getName();

    private DataProvider() {
    }

    public static final Uri CUSTOMQUERY = Uri.parse("content://" + AUTHORITY
            + "/customquery");

    public static final class EnglishMaster implements BaseColumns {

        private EnglishMaster() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/englishmaster");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.demo.englishmaster";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.demo.englishmaster";
        public static final String ID = "_id";
        public static final String KEYWORD = "keyword";
        public static final String COUNT = "count";
        public static final String OTHER = "other";
    }

    public static final class EnglishSuggest implements BaseColumns {
        private EnglishSuggest() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/englishsuggest");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.demo.englishsuggest";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.demo.englishsuggest";
        public static final String ID = "_id";// auto increment
        public static final String KEYWORD = "keyword";
        public static final String COUNT = "count";
        public static final String SYNC_FLAG = "sync_flag";
    }
}
