package com.mitechlt.tvportal.play.model;

public class Mirror {

    /**
     * The title of the mirror
     */
    public String title;

    /**
     * The link to this episode or movie's mirrors
     */
    public String link;

    /**
     * A boolean to determine whether chromecast streaming is available.
     */
    public boolean chromecast;

    /**
     * @param title the title of the mirror
     * @param link  the link to this episode or movie's mirrors
     * @param chromecast  the link to this episode or movie's mirrors
     */
    public Mirror(String title, String link, boolean chromecast) {
        this.title = title;
        this.link = link;
        this.chromecast = chromecast;
    }

}
