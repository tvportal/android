package com.mitechlt.tvportal.play.model;

public class Movie {

    /**
     * The title of the movie
     */
    public String title;

    /**
     * The link to the movie detail page
     */
    public String link;

    /**
     * The link to the movie's image
     */
    public String imageUri;

    /**
     * The rating of this movie (out of 100)
     */
    public int rating;

    /**
     * The year this movie was released
     */
    public String year;

    /**
     * The series of "|" separated genres that this movie belongs to
     */
    public String genres;


    /**
     * @param title    the title of the movie
     * @param link     the link to the movie detail page
     * @param imageUri the link to the movie's image
     * @param rating   the rating of this movie
     * @param year     the year this movie was released
     * @param genres   the series of "|" separated genres that this movie belongs to
     */
    public Movie(String title, String link, String imageUri, int rating, String year, String genres
    ) {
        this.title = title;
        this.link = link;
        this.imageUri = imageUri;
        this.rating = rating;
        this.year = year;
        this.genres = genres;
    }
}
