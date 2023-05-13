package com.example.lasttp;

public class Music {
    private String music;
    private boolean isFavorite;

    public Music(String music, boolean isFavorite) {
        this.music = music;
        this.isFavorite = isFavorite;
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
}
