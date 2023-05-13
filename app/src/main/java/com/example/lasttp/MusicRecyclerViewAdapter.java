package com.example.lasttp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;





public class  MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Music> songs =new ArrayList<>();

    public MusicRecyclerViewAdapter(Context context) {
        this.context=context;
    }

    @NonNull
    @Override
    public MusicRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.music_list_item,parent,false);
        ViewHolder holder= new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.txtMusic.setText(songs.get(position).getMusic());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,MainActivity.class);
                intent.putExtra("musicName",songs.get(position).getMusic());
                intent.putExtra("isFavorite",songs.get(position).isFavorite());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtMusic;
        private CardView parent;
        private ImageView imageViewMusic;
        private ImageView imageViewHeart;
        private ImageView imageViewEmptyHeart;
        private ImageView imageViewPlay;
        private ImageView imageViewPause;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMusic =itemView.findViewById(R.id.textViewMusicListItem);
            parent =itemView.findViewById(R.id.parent);
            imageViewMusic =itemView.findViewById(R.id.imageViewMusicListItem);
            imageViewHeart =itemView.findViewById(R.id.imageViewHeartListItem);
            imageViewEmptyHeart =itemView.findViewById(R.id.imageViewHeartEmptyListItem);
            imageViewPlay =itemView.findViewById(R.id.imageViewPlayListItem);
            imageViewPause =itemView.findViewById(R.id.imageViewPauseListItem);

        }
    }
    public ArrayList<Music> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Music> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }



}
