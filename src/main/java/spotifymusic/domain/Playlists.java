package spotifymusic.domain;

import java.util.List;

public class Playlists extends SpotifyContainer{
    List<Playlist> items;

    public List<Playlist> getItems() {
        return items;
    }
}
