package com.example.lasttp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Music{
    private int id;
    private String music;
    private boolean isFavorite;
    private String musicUri;

    public Music(String music, String musicUri) {
        this.music = music;
        this.musicUri = musicUri;
    }
    public Music( String musicUri) {
        this.musicUri = musicUri;
    }
    public Music(int id, String music, String musicUri) {
        this.id = id;
        this.music = music;
        this.musicUri = musicUri;
    }




    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getMusicUri() {
        return musicUri;
    }

    public void setMusicUri(String musicUri) {
        this.musicUri = musicUri;
    }





}
