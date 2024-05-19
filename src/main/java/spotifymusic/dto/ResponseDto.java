package spotifymusic.dto;

import spotifymusic.domain.SpotifyContainer;

public class ResponseDto {
    int status;
    String message;
    SpotifyContainer body;

    public ResponseDto(int status, SpotifyContainer body) {
        this.status = status;
        this.message = "";
        this.body = body;
    }

    public ResponseDto(int status, String message) {
        this.status = status;
        this.message = message;
        this.body = null;
    }

    public int getStatus() {
        return status;
    }

    public SpotifyContainer getBody() {
        return body;
    }

    public String getMessage() {
        return message;
    }
}
