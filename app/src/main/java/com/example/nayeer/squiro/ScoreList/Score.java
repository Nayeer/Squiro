package com.example.nayeer.squiro.ScoreList;

public class Score {

    protected String pseudo;
    protected int score;
    protected Double latitude, longitude;

    public Score(int score, String pseudo , Double longitude, Double latitude) {
        this.score = score;
        this.pseudo = pseudo;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getScore() {
        return score;
    }
    public String getPseudo() {
        return pseudo;
    }
    public Double getLatitude() {return latitude;}
    public Double getLongitude() {return longitude;}
}

