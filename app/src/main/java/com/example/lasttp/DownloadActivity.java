package com.example.lasttp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class DownloadActivity extends AppCompatActivity {
    private Button btnDownload;
    private EditText editText;
    private TextView textView;

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "download_channel";

    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        btnDownload = findViewById(R.id.btnDownload);
        editText = findViewById(R.id.editTextTextSongName);
        textView = findViewById(R.id.idMusicName);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String songUrl = editText.getText().toString();
                DownloadTask downloadTask = new DownloadTask(DownloadActivity.this, songUrl);
                downloadTask.execute();
            }
        });
    }

    public void pauseResumeMusic(View view) {
        if (mediaPlayer != null) {
            if (isMusicPlaying) {
                mediaPlayer.pause();
                isMusicPlaying = false;
            } else {
                mediaPlayer.start();
                isMusicPlaying = true;
            }
        }
    }

    private class DownloadTask extends AsyncTask<Void, Integer, String> {
        private Context context;
        private NotificationManagerCompat notificationManager;
        private NotificationCompat.Builder notificationBuilder;
        private String songUrl;

        public DownloadTask(Context context, String songUrl) {
            this.context = context;
            this.songUrl = songUrl;
            notificationManager = NotificationManagerCompat.from(context);
            createNotificationChannel();
            notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.download)
                    .setContentTitle("Download in progress")
                    .setContentText("Downloading song...")
                    .setPriority(NotificationCompat.PRIORITY_LOW);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (ActivityCompat.checkSelfPermission(DownloadActivity.this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Request the missing permission
                return;
            }
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(songUrl)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        // Determine the total file size for progress calculation
                        long totalFileSize = responseBody.contentLength();

                        // Create a file output stream to save the downloaded file
                        File outputFile = new File(getFilesDir(), "song.mp3");
                        BufferedSink bufferedSink = Okio.buffer(Okio.sink(outputFile));

                        // Create a buffer to read the response body
                        byte[] buffer = new byte[4096];

                        // Read the response body and write to the file output stream
                        BufferedSource bufferedSource = responseBody.source();
                        long bytesRead;
                        long totalBytesRead = 0;
                        while ((bytesRead = bufferedSource.read(buffer)) != -1) {
                            bufferedSink.write(buffer, 0, (int) bytesRead);
                            totalBytesRead += bytesRead;

                            // Calculate the progress percentage
                            int progress = (int) ((totalBytesRead * 100) / totalFileSize);

                            // Publish the progress to onProgressUpdate()
                            publishProgress(progress);
                        }

                        // Close the streams
                        bufferedSink.flush();
                        bufferedSink.close();
                        bufferedSource.close();

                        // Return the downloaded file path
                        return outputFile.getAbsolutePath();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File outputFile = new File(externalDir, "song.mp3");
            try {
                BufferedSink bufferedSink = Okio.buffer(Okio.sink(outputFile));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            // Update the progress of the notification
            notificationBuilder.setProgress(100, progress, false);
            if (ActivityCompat.checkSelfPermission(DownloadActivity.this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Request the missing permission
                return;
            }
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }

        @Override
        protected void onPostExecute(String filePath) {
            super.onPostExecute(filePath);

            // Download completed, update the notification
            notificationBuilder.setContentText("Download completed")
                    .setProgress(0, 0, false)
                    .setContentIntent(createPendingIntent(filePath)); // Set the content intent

            if (ActivityCompat.checkSelfPermission(DownloadActivity.this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling ActivityCompat#requestPermissions here to request the missing permissions
                return;
            }

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

            // Play the downloaded song
            if (filePath != null) {
                playSong(filePath);
            }
        }

        private void playSong(String filePath) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                isMusicPlaying = true;
                textView.setText(getFileNameFromUrl(songUrl));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private PendingIntent createPendingIntent(String filePath) {
            Intent playIntent = new Intent(DownloadActivity.this, DetailMusic.class);
            playIntent.putExtra("songFilePath", filePath);

            // Create a unique request code for the pending intent
            int requestCode = (int) System.currentTimeMillis();

            return PendingIntent.getActivity(DownloadActivity.this, requestCode, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        private void createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Download Channel";
                String description = "Channel for download notifications";
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }

        private String getFileNameFromUrl(String url) {
            String[] segments = url.split("/");
            String fileNameWithExtension = segments[segments.length - 1];
            int dotIndex = fileNameWithExtension.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < fileNameWithExtension.length() - 1) {
                return fileNameWithExtension.substring(0, dotIndex);
            }
            return fileNameWithExtension;
        }
    }
}
