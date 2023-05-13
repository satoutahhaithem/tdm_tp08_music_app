package com.example.lasttp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class MusicHome extends AppCompatActivity {
    private RecyclerView recyclerView;
    ArrayList<Music> songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_home);
        recyclerView = findViewById(R.id.recyclerView);
        songs=new ArrayList<>();
        songs.add(new Music("first  song",true));
        songs.add(new Music("second song",true));
        MusicRecyclerViewAdapter musicRecyclerViewAdapter = new MusicRecyclerViewAdapter(this);
        musicRecyclerViewAdapter.setSongs(songs);
        recyclerView.setAdapter(musicRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}