package com.mitechlt.tvportal.play.model;

public class TVShow {

    /**
     * The title of the series
     */
    public String title;

    /**
     * The link to the series detail page
     */
    public String link;

    /**
     * The link to the tv show's image
     */
    public String imageUri;

    /**
     * The rating of this tv show (out of 100)
     */
    public int rating;

    /**
     * The year this tv show was released
     */
    public String year;

    /**
     * The series of "|" separated genres that tv show belongs to
     */
    public String genres;

    /**
     * @param title    the title of the series
     * @param link     the link to the series detail page
     * @param imageUri the link to the tv show's image
     * @param rating   the rating of this tv show
     * @param year     the year this tv show was released
     * @param genres   the series of "|" separated genres that this tv show belongs to
     */
    public TVShow(String title, String link, String imageUri, int rating, String year, String genres) {
        this.title = title;
        this.link = link;
        this.imageUri = imageUri;
        this.rating = rating;
        this.year = year;
        this.genres = genres;
    }

}
