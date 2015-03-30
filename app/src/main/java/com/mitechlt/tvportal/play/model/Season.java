package com.mitechlt.tvportal.play.model;

public class Season {

    /**
     * The title of the season
     */
    public String title;

    /**
     * The link to this season's episodes
     */
    public String link;


    /**
     * @param title the title of the season
     * @param link  the link to this season's episodes
     */
    public Season(String title, String link) {
        this.title = title;
        this.link = link;
    }

}
