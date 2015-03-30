package com.mitechlt.tvportal.play.model;

public class Episode {

    /**
     * The title of the episode
     */
    public String title;

    /**
     * The series this episode belongs to
     */
    public String series;

    /**
     * The link to the episode
     */
    public String link;

    /**
     * The title of the episode
     */
    public String episode;

    /**
     * The title of the season
     */
    public String season;

    /**
     * Boolean to indicate whether this episode has been watched
     */
    public boolean watched;

    /**
     * @param title the title of the episode
     * @param link  the link to the episode
     */
    public Episode(String title, String series, String link, String episode, String season, boolean watched) {
        this.title = title;
        this.link = link;
        this.episode = episode;
        this.season = season;
        this.watched = watched;
        this.series = series;
    }

}
