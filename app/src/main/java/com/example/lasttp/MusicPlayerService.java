package com.example.lasttp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayerService extends Service {

    private MediaPlayer mediaPlayer;
    private static final String TAG = "MusicPlayerService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "com.example.lasttp.music_player";
     static final String ACTION_PLAY = "com.example.lasttp.PLAY_ACTION";
     static final String ACTION_PAUSE = "com.example.lasttp.PAUSE_ACTION";
     static final String ACTION_STOP = "com.example.lasttp.STOP_ACTION";
     static final String ACTION_RESUME = "com.example.lasttp.RESUME_ACTION";
     static final String ACTION_NEXT = "com.example.lasttp.NEXT_ACTION";
     static final String ACTION_PREVIOUS = "com.example.lasttp.PREVIOUS_ACTION";

    private ArrayList<Music> songs = new ArrayList<>();
    private int currentPosition = -1;
    private boolean isPlaying = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String musicUri = intent.getStringExtra("musicUri");
        int pos = intent.getIntExtra("position", -1);
        System.out.println(pos);
        System.out.println("songs size "+songs.size());
        System.out.println("the position"+currentPosition);

        if (action != null) {
            switch (action) {
                case ACTION_PLAY:
                    if (musicUri != null) {
                        startPlayback(musicUri);
                    }
                    if (pos!=-1){
                        currentPosition=pos;
                    }
                    break;
                case ACTION_PAUSE:
                    pausePlayback();
                    if (pos!=-1){
                        currentPosition=pos;
                    }
                    break;
                case ACTION_STOP:
                    stopPlayback();
                    break;
                case ACTION_RESUME:
                    resumePlayback();
                    if (pos!=-1){
                        currentPosition=pos;
                    }
                    break;
                case ACTION_NEXT:
                    if (pos!=-1){
                        currentPosition=pos;
                    }
                    if (currentPosition!=-1){
                        nextPlayback(currentPosition);
                    }
                    break;
                case ACTION_PREVIOUS:
                    if (pos!=-1){
                        currentPosition=pos;
                    }
                    if (currentPosition!=-1){
                        previousPlayback(currentPosition);
                    }
                    break;
            }
        }

        showNotification();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadMusicFromLocalStorage();
    }

    public void playNextSong() {
        System.out.println("the size of the songs is "+songs.size());
        if (currentPosition < songs.size() - 1) {
            currentPosition++;
            String musicUri = songs.get(currentPosition).getMusicUri();
            if (musicUri != null && !musicUri.isEmpty()) {
                stopPlayback();
                startPlayback(musicUri);
            }
        }
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

    private void startPlayback(String musicUri) {
        stopPlayback(); // Stop any existing playback

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(musicUri));
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Play the next song after the current song finishes
                    playNextSong();
                }
            });

            // Update the currentPlayingPosition and isPlaying state
            currentPosition = songs.indexOf(new Music(musicUri)); // Update the currentPosition based on the musicUri
            isPlaying = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextPlayback(int pos) {
        stopPlayback(); // Stop any existing playback

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            System.out.println("inside next play back "+songs.size());
            System.out.println("_____________________________________________");
            System.out.println("position "+pos);
            System.out.println("_____________________________________________");
            if (currentPosition!=songs.size()-1){
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(songs.get(currentPosition+1).getMusicUri()));
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Play the next song after the current song finishes
                    playNextSong();
                }
            });

            // Update the currentPlayingPosition and isPlaying state
            if(currentPosition!=songs.size()-1){
                currentPosition = currentPosition+1; // Update the currentPosition based on the musicUri
                isPlaying = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void previousPlayback(int pos) {
        stopPlayback(); // Stop any existing playback

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            System.out.println("inside next play back "+songs.size());
            System.out.println("_____________________________________________");
            System.out.println("position "+pos);
            System.out.println("_____________________________________________");
            if(currentPosition!=0){
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(songs.get(currentPosition-1).getMusicUri()));
                mediaPlayer.prepare();
                mediaPlayer.start();
            }


            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Play the next song after the current song finishes
                    playNextSong();
                }
            });

            // Update the currentPlayingPosition and isPlaying state
            if(currentPosition!=0){
                currentPosition = currentPosition-1; // Update the currentPosition based on the musicUri
                isPlaying = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    private void resumePlayback() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    private void showNotification() {
        // Create a notification channel for Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music Player", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Music Player");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.music)
                .setContentTitle("Lecture en cours")
                .setContentText("Now playing: "+songs.get(currentPosition).getMusic());

        // Create the intent for the notification
        Intent intent = new Intent(this, MusicHome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);





        // Add the previous button to the notification
        builder.addAction(R.drawable.ic_launcher_background, "Previous", createPlaybackAction(ACTION_PREVIOUS));

        // Add the play/pause button to the notification
        if (isPlaying) {
            builder.addAction(R.drawable.pause, "Pause", createPlaybackAction(ACTION_PAUSE));
        } else {
            builder.addAction(R.drawable.play, "Play", createPlaybackAction(ACTION_RESUME));
        }
        // Add the next button to the notification
        builder.addAction(R.drawable.forward, "Next", createPlaybackAction(ACTION_NEXT));


        // Build and display the notification
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private PendingIntent createPlaybackAction(String action) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, 0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}