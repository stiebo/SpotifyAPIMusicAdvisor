package spotifymusic.domain;

public class Playlist {
    Urls external_urls;
    String name;

    @Override
    public String toString() {
        return name + "\n" +
                external_urls + "\n";
    }
}
