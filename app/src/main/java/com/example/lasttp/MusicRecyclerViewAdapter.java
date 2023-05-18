package com.example.lasttp;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.Hold;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Music> songs = new ArrayList<>();
    private  ArrayList<Music> favoriteSongs = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private int currentPlayingPosition = -1;
    boolean isFavorite;
    private boolean isPlaying = false;
    private int currentPosition = -1;
    private boolean isFavoriteActivity;
    private static final String TAG = "MusicRecyclerViewAdapte";

    public MusicRecyclerViewAdapter(Context context,boolean isFavoriteActivity) {
        this.context = context;
        this.isFavoriteActivity = isFavoriteActivity;
    }

    @NonNull
    @Override
    public MusicRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicRecyclerViewAdapter.ViewHolder holder, int position) {

        holder.txtMusic.setText(songs.get(position).getMusic());


        // Check if the current song is a favorite by querying the database
        MusicDatabaseHelper dbHelper = MusicDatabaseHelper.getInstance(context);
        favoriteSongs = (ArrayList<Music>) dbHelper.getFavoriteSongsFromDatabase();
        isFavorite = isSongFavorite(favoriteSongs, songs.get(position));

        if (isFavorite) {
            // Display the heart icon as filled
            holder.imageViewHeartListItem.setVisibility(View.VISIBLE);
        } else {
            // Display the heart icon as empty
            holder.imageViewHeartListItem.setVisibility(View.GONE);
        }

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,DetailMusic.class);
                intent.putExtra("songName",songs.get(position).getMusic());
                intent.putExtra("isFavorite",isFavorite);
                Log.d(TAG, "onClick: isFavorite value "+isFavorite);
                intent.putExtra("position",position);
                context.startActivity(intent);

                int position = holder.getAdapterPosition();
                String musicUri = songs.get(position).getMusicUri();

                if (musicUri != null && !musicUri.isEmpty()) {
                    if (currentPosition == position && isPlaying) {
                        // Pause the playback
                        Intent intent1 = new Intent(context, MusicPlayerService.class);
                        intent1.setAction(MusicPlayerService.ACTION_PAUSE);
                        context.startService(intent1);
                        isPlaying = false;
                    } else {
                        // Start playing the selected song
                        Intent intent2 = new Intent(context, MusicPlayerService.class);
                        intent2.setAction(MusicPlayerService.ACTION_PLAY);
                        intent2.putExtra("musicUri", musicUri);
                        intent2.putExtra("position",position);
                        System.out.println("recycler view songs(0)"+songs.get(0));
                        context.startService(intent2);
                        currentPosition = position;
                        isPlaying = true;
                    }
                    notifyDataSetChanged();
                }
            }
        });







        holder.imageViewHeartEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.imageViewHeartListItem.setVisibility(View.VISIBLE);
                MusicDatabaseHelper dbHelper = MusicDatabaseHelper.getInstance(context);
                System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkk");
                dbHelper.saveToDatabase(songs.get(position));
                System.out.println("fhsdjkhglbfjld"+songs.get(position));
                ArrayList<Music> favoriteSongss = (ArrayList<Music>) dbHelper.getFavoriteSongsFromDatabase();
                System.out.println("the first favorite songs " + favoriteSongss.size());
            }
        });

        holder.imageViewHeartListItem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                holder.imageViewHeartListItem.setVisibility(View.GONE);
                MusicDatabaseHelper dbHelper = MusicDatabaseHelper.getInstance(context);
                dbHelper.removeFromDatabase(songs.get(position));
                MusicDatabaseHelper musicDatabaseHelper = new MusicDatabaseHelper(context);
                ArrayList<Music> favoriteSongss = (ArrayList<Music>) musicDatabaseHelper.getFavoriteSongsFromDatabase();
                System.out.println("the first favorite songs " + favoriteSongss.size());
                if (isFavoriteActivity){
                    songs.remove(position);
                    notifyDataSetChanged();
                }
            }
        });




    }

    private boolean isSongFavorite(List<Music> favoriteSongs, Music song) {
        for (Music favoriteSong : favoriteSongs) {
            if (favoriteSong.getMusicUri().equals(song.getMusicUri())) {
                return true;
            }
        }
        return false;
    }
    public int getNextPosition() {
        // Increment the currentPlayingPosition by 1 to get the next position
        int nextPosition = currentPlayingPosition + 1;

        // Check if the nextPosition is within the bounds of the songs list
        if (nextPosition >= 0 && nextPosition < songs.size()) {
            return nextPosition;
        } else {
            // If the nextPosition is out of bounds, wrap around to the beginning of the list
            return 0;
        }
    }

    public void updateFavoriteSongs() {
        MusicDatabaseHelper musicDatabaseHelper = new MusicDatabaseHelper(context);
        ArrayList<Music> favoriteSongs = (ArrayList<Music>) musicDatabaseHelper.getFavoriteSongsFromDatabase();
        setFavoriteSongs(favoriteSongs);
    }

    public void playNextSong() {
        // Get the next position
        int nextPosition = getNextPosition();

        // Check if the next position is valid
        if (nextPosition >= 0 && nextPosition < songs.size()) {
            // Stop the current playback
            stopPlayback();

            // Get the music URI of the next song
            String musicUri = songs.get(nextPosition).getMusicUri();

            if (musicUri != null && !musicUri.isEmpty()) {
                // Start playing the next song
                Intent intent = new Intent(context, MusicPlayerService.class);
                intent.setAction(MusicPlayerService.ACTION_PLAY);
                intent.putExtra("musicUri", musicUri);
                context.startService(intent);

                // Update the currentPlayingPosition
                currentPlayingPosition = nextPosition;
            }
        }
    }
    private void stopPlayback() {
        // Stop the playback in the MusicPlayerService
        Intent intent = new Intent(context, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_STOP);
        context.startService(intent);
    }


    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtMusic;
        private CardView parent;

        private ImageView imageViewHeartListItem;
        private ImageView imageViewHeartEmpty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMusic = itemView.findViewById(R.id.textViewMusicListItem);
            parent = itemView.findViewById(R.id.parent);
            imageViewHeartListItem = itemView.findViewById(R.id.imageViewHeartListItem);
            imageViewHeartEmpty = itemView.findViewById(R.id.imageViewHeartEmptyListItem);
            imageViewHeartListItem.setVisibility(View.GONE);


        }
    }

    public ArrayList<Music> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Music> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }
    public void setFavoriteSongs(ArrayList<Music> songs) {
        this.favoriteSongs = songs;
        notifyDataSetChanged();
    }
    private void stopMediaPlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }





}
