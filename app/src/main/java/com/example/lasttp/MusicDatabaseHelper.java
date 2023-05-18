

package com.example.lasttp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MusicDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = MusicDatabaseHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "music_database";
    private static final int DATABASE_VERSION = 1;
    private static MusicDatabaseHelper instance;

    private static final String TABLE_NAME = "music";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MUSIC = "music";
    private static final String COLUMN_MUSIC_URI = "musicUri";
    private static final String COLUMN_IS_FAVORITE = "isFavorite";

    MusicDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized MusicDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MusicDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the database table
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MUSIC + " TEXT,"
                + COLUMN_MUSIC_URI + " TEXT,"
                + COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public void saveToDatabase(Music music) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MUSIC, music.getMusic());
        values.put(COLUMN_MUSIC_URI, music.getMusicUri());
        values.put(COLUMN_IS_FAVORITE, 1); // 1 for true, 0 for false

        long id = db.insert(TABLE_NAME, null, values);
        Log.d(TAG, "saveToDatabase: New record inserted with ID: " + id);
        db.close();
    }

    public void removeFromDatabase(Music music) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, COLUMN_MUSIC_URI + " = ?", new String[]{music.getMusicUri()});
        Log.d(TAG, "removeFromDatabase: Removed " + result + " record(s)");

        db.close();
    }

    public List<Music> getFavoriteSongsFromDatabase() {
        List<Music> favoriteSongs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_MUSIC + ", " + COLUMN_MUSIC_URI + " FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int musicIndex = cursor.getColumnIndex(COLUMN_MUSIC);
            int musicUriIndex = cursor.getColumnIndex(COLUMN_MUSIC_URI);

            do {
                int id = cursor.getInt(idIndex);
                String music = cursor.getString(musicIndex);
                String musicUri = cursor.getString(musicUriIndex);
                favoriteSongs.add(new Music(id, music, musicUri));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return favoriteSongs;
    }
}

