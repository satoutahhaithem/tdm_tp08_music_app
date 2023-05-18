package com.example.lasttp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicHome extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 100;

    private RecyclerView recyclerView;
    private List<Music> songs;
    private MusicRecyclerViewAdapter musicRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_home);

        recyclerView = findViewById(R.id.recyclerView);


        songs = new ArrayList<>();
        musicRecyclerViewAdapter = new MusicRecyclerViewAdapter(this,false);
        musicRecyclerViewAdapter.setSongs((ArrayList<Music>) songs);
        recyclerView.setAdapter(musicRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        // Request the READ_EXTERNAL_STORAGE permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // Permission is already granted, load the music files
            loadMusicFromLocalStorage();
        }






    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, load the music files
                loadMusicFromLocalStorage();
            } else {
                // Permission is denied, handle it accordingly (e.g., display a message or disable the feature)
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favorite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_favorite) {
            Intent intent = new Intent(this, FavoriteSongsActivity.class);
            startActivity(intent);
            return true;
        }
        if (itemId == R.id.action_download) {
            Intent intent = new Intent(this,DownloadActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadMusicFromLocalStorage() {
        songs.clear();

        // Query the media store to retrieve music files
        String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA};
        Uri musicContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = getContentResolver().query(musicContentUri, projection, selection, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                Uri uri = Uri.fromFile(new File(path));
                songs.add(new Music(title, uri.toString()));
            } while (cursor.moveToNext());

            cursor.close();
        }

        musicRecyclerViewAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Update the favorite songs list when the activity comes into the foreground
        musicRecyclerViewAdapter.updateFavoriteSongs();
        musicRecyclerViewAdapter.notifyDataSetChanged();
    }


}



