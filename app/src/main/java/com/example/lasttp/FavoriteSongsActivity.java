package com.example.lasttp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class FavoriteSongsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewFavoriteSongs;
    private MusicRecyclerViewAdapter musicRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_songs);
        recyclerViewFavoriteSongs=findViewById(R.id.recylcerViewFavoriteSongs);
        MusicDatabaseHelper musicDatabaseHelper = new MusicDatabaseHelper(this);
        ArrayList<Music> favoriteSongs = (ArrayList<Music>) musicDatabaseHelper.getFavoriteSongsFromDatabase();



        musicRecyclerViewAdapter = new MusicRecyclerViewAdapter(this,true);
        musicRecyclerViewAdapter.setSongs((ArrayList<Music>) favoriteSongs);

        recyclerViewFavoriteSongs.setAdapter(musicRecyclerViewAdapter);
        recyclerViewFavoriteSongs.setLayoutManager(new LinearLayoutManager(this));

    }
    @Override
    public void onBackPressed() {
        musicRecyclerViewAdapter.updateFavoriteSongs();
        super.onBackPressed();
    }
}