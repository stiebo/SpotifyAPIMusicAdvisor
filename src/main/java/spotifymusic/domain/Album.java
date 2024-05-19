package spotifymusic.domain;

import java.util.Arrays;

public class Album {
    private Urls external_urls;
    private String name;
    private Artist[] artists;

    @Override
    public String toString() {
        return name + "\n" +
                Arrays.toString(artists) + "\n" +
                external_urls + "\n";
    }
}
