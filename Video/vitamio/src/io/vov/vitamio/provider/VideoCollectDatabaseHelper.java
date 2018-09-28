package io.vov.vitamio.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by MR.XIE on 2018/9/16.
 */
public class VideoCollectDatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;
    private final String TAG="movie2";
    public VideoCollectDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
     sqLiteDatabase.execSQL("create table if not exists VideoCollect(VIDEO_PATH  text primary key not null," +
             "VIDEO_NAME text not null,VIDEO_THUMBNAIL blob,VIDEO_THUMBNAIL_PATH text,VIDEO_SIZE real not null,VIDEO_PROGRESS integer not null," +
             "VIDEO_RESOLUTION text,VIDEO_DATE real not null,VIDEO_DURATION real not null);");
     sqLiteDatabase.execSQL("create table if not exists VideoDownload(VIDEO_SAVE_PATH text primary key not null," +
             "VIDEO_DOWNLOAD_PATH text not null);");
        Log.i(TAG, "onCreate: 创建成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists VideoCollect");
        sqLiteDatabase.execSQL("drop table if exists VideoDownload");
         onCreate(sqLiteDatabase);
        Log.i(TAG, "onCreate: 创建成功");
    }
}
