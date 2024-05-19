package spotifymusic.domain;

import java.util.List;

public abstract class SpotifyContainer {
    private int limit;
    private String next;
    private int offset;
    private String previous;
    private int total;

    public abstract List<?> getItems();

    public int getLimit() {
        return limit;
    }

    public String getNext() {
        return next;
    }

    public int getOffset() {
        return offset;
    }

    public String getPrevious() {
        return previous;
    }

    public int getTotal() {
        return total;
    }
}
