package com.example.lasttp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetailMusic extends AppCompatActivity {
    private TextView textViewSongName;
    private String songName;
    private boolean isFavorite=false;
    private ImageView imageViewHeart;
    private ImageView imageViewEmptyHeart;
    private ImageView imageViewPlay;
    private ImageView imageViewPause;
    private ImageView imageViewNext;
    private ImageView imageViewPrevious;
    private int position=-1;
    private List<Music> songs;
    private List<Music> favoriteSongs;


    private static final String TAG = "DetailMusic";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_music);
        songs=new ArrayList<>();
        loadMusicFromLocalStorage();
        Log.d(TAG, "onCreate: song size is "+songs.size());

        textViewSongName = findViewById(R.id.idSongName);
        imageViewHeart=findViewById(R.id.idHeart);
        imageViewEmptyHeart=findViewById(R.id.idEmptyHeart);
        imageViewPlay=findViewById(R.id.idPlay);
        imageViewPause=findViewById(R.id.idPause);
        imageViewNext=findViewById(R.id.idNext);
        imageViewPrevious=findViewById(R.id.idPrevious);

        Intent intent = getIntent();
        songName= intent.getStringExtra("songName");
        position= intent.getIntExtra("position",-1);
        MusicDatabaseHelper dbHelper = MusicDatabaseHelper.getInstance(this);
        favoriteSongs = (ArrayList<Music>) dbHelper.getFavoriteSongsFromDatabase();
        isFavorite = isSongFavorite(favoriteSongs, songs.get(position));
        Log.d(TAG, "onCreate: is Favorite value detail "+isFavorite);
        textViewSongName.setText(songName);

        if (isFavorite){
            imageViewHeart.setVisibility(View.VISIBLE);
        }
        imageViewPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewPause.setVisibility(View.GONE);
                pauseMusic(v);
            }
        });
        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewPause.setVisibility(View.VISIBLE);
                resumeMusic(v);
            }
        });
        imageViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewPause.setVisibility(View.VISIBLE);
                nextMusic(v);

                textViewSongName.setText(songs.get(position).getMusic());
            }
        });
        imageViewPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewPause.setVisibility(View.VISIBLE);
                previousMusic(v);

                textViewSongName.setText(songs.get(position).getMusic());
            }
        });
        imageViewEmptyHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewHeart.setVisibility(View.VISIBLE);
                MusicDatabaseHelper dbHelper = MusicDatabaseHelper.getInstance(v.getContext());
                System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkk");
                dbHelper.saveToDatabase(songs.get(position));
                System.out.println("fhsdjkhglbfjld"+songs.get(position));
                ArrayList<Music> favoriteSongss = (ArrayList<Music>) dbHelper.getFavoriteSongsFromDatabase();
                System.out.println("the first favorite songs " + favoriteSongss.size());
            }
        });
        imageViewHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewHeart.setVisibility(View.GONE);
                MusicDatabaseHelper dbHelper = MusicDatabaseHelper.getInstance(v.getContext());
                dbHelper.removeFromDatabase(songs.get(position));
                MusicDatabaseHelper musicDatabaseHelper = new MusicDatabaseHelper(v.getContext());
                ArrayList<Music> favoriteSongss = (ArrayList<Music>) musicDatabaseHelper.getFavoriteSongsFromDatabase();
                System.out.println("the first favorite songs " + favoriteSongss.size());
            }
        });

    }


    public void pauseMusic(View view) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_PAUSE);
        startService(intent);
    }
    public void resumeMusic(View view) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_RESUME);
        startService(intent);
    }
    public void nextMusic(View view) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_NEXT);
        startService(intent);
        if(position!=songs.size()-1){
            position=position+1;
        }

        updateIcon(view);


    }
    public void previousMusic(View view) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_PREVIOUS);
        startService(intent);
        if(position!=0){
            position=position-1;
        }
        updateIcon(view);
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


    }
    private boolean isSongFavorite(List<Music> favoriteSongs, Music song) {
        for (Music favoriteSong : favoriteSongs) {
            if (favoriteSong.getMusicUri().equals(song.getMusicUri())) {
                return true;
            }
        }
        return false;
    }

    private void updateIcon(View view){
        MusicDatabaseHelper dbHelper = MusicDatabaseHelper.getInstance(view.getContext());
        ArrayList<Music> favoriteSongss = (ArrayList<Music>) dbHelper.getFavoriteSongsFromDatabase();
        if (isSongFavorite(favoriteSongss,songs.get(position))){
            imageViewHeart.setVisibility(View.VISIBLE);
        }else{
            imageViewHeart.setVisibility(View.GONE);
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
        return super.onOptionsItemSelected(item);
    }


}