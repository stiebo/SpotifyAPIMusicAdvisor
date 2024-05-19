package spotifymusic.model;

import spotifymusic.domain.*;
import spotifymusic.domain.Error;
import spotifymusic.dto.ResponseDto;
import com.google.gson.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpotifyModel {
    private final String API_PATH_NEW_RELEASES = "/v1/browse/new-releases?offset=0&limit=%d";
    private final String API_PATH_FEATURED = "/v1/browse/featured-playlists?offset=0&limit=%d";
    private final String API_PATH_CATEGORIES = "/v1/browse/categories?offset=0&limit=%d";
    private final String API_PATH_PLAYLIST = "/v1/browse/categories/%s/playlists?offset=0&limit=%d";
    private final String apiServerPath = "https://api.spotify.com";
    private final Gson gson;
    private final int pageLimit;

    public SpotifyModel(int pageLimit) {
        this.gson = new GsonBuilder().create();
        this.pageLimit = pageLimit;
    }

    private JsonObject getRequest(String accessToken, String apiRequestPath) {
        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .uri(URI.create(apiRequestPath))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseDto getNewReleases(String accessToken, String requestPath) {
        if (requestPath == null) {
            requestPath = apiServerPath + API_PATH_NEW_RELEASES.formatted(pageLimit);
        }
        JsonObject jsonResponse = getRequest(accessToken, requestPath);
        if (jsonResponse.has("error")) {
            Error error = gson.fromJson(jsonResponse, Error.class);
            return new ResponseDto(error.getStatus(), error.getMessage());
        }
        Albums albums = gson.fromJson(jsonResponse.getAsJsonObject("albums"),
                Albums.class);
        return new ResponseDto(200, albums);
    }

    public ResponseDto getFeaturedPlaylists(String accessToken, String requestPath) {
        if (requestPath == null) {
            requestPath = apiServerPath + API_PATH_FEATURED.formatted(pageLimit);
        }
        JsonObject jsonResponse = getRequest(accessToken, requestPath);
        if (jsonResponse.has("error")) {
            Error error = gson.fromJson(jsonResponse, Error.class);
            return new ResponseDto(error.getStatus(), error.getMessage());
        }
        Playlists playlists = gson.fromJson(jsonResponse.getAsJsonObject("playlists"),
                Playlists.class);
        return new ResponseDto(200, playlists);
    }

    public ResponseDto getCategories(String accessToken, String requestPath, Optional<Integer> limit) {
        int myPageLimit = limit.orElse(pageLimit);
        if (requestPath == null) {
            requestPath = apiServerPath + API_PATH_CATEGORIES.formatted(myPageLimit);
        }
        JsonObject jsonResponse = getRequest(accessToken, requestPath);
        if (jsonResponse.has("error")) {
            Error error = gson.fromJson(jsonResponse.getAsJsonObject("error"), Error.class);
            return new ResponseDto(error.getStatus(), error.getMessage());
        }
        Categories categories = gson.fromJson(jsonResponse.getAsJsonObject("categories"),
                Categories.class);
        return new ResponseDto(200, categories);
    }

    public ResponseDto getPlaylistsForCategory(String catName, String accessToken, String requestPath)
            throws NullPointerException {

        if (requestPath == null) {
            List<Category> categories = new ArrayList<>();
            String urlNext = null;
            do {
                ResponseDto categoriesDto = getCategories(accessToken, urlNext, Optional.of(50));
                if (categoriesDto.getStatus() != 200) {
                    return new ResponseDto(categoriesDto.getStatus(), categoriesDto.getMessage());
                }
                Categories container = (Categories) categoriesDto.getBody();
                categories.addAll(container.getItems());
                urlNext = container.getNext();

            } while (urlNext != null);

            String catId = categories.stream()
                    .filter(cat -> cat.getName().equals(catName))
                    .map(Category::getId)
                    .findFirst()
                    .orElse(null);
            if (catId == null) {
                return new ResponseDto(404, "Unknown category name");
            }
            requestPath = apiServerPath + API_PATH_PLAYLIST.formatted(catId, pageLimit);
        }

        JsonObject jsonResponse = getRequest(accessToken, requestPath);
        if (jsonResponse.has("error")) {
            Error error = gson.fromJson(jsonResponse, Error.class);
            return new ResponseDto(error.getStatus(), error.getMessage());
        }
        Playlists playlists = gson.fromJson(jsonResponse.getAsJsonObject("playlists"),
                Playlists.class);
        return new ResponseDto(200, playlists);
    }
}