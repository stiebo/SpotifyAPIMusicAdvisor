package spotifymusic.controller;

import spotifymusic.view.SpotifyView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.net.URI;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class SpotifyAuthorization {
    private final SpotifyView view;
    private final String serverPath = "https://accounts.spotify.com";
    // How to create an app on spotify: https://developer.spotify.com/documentation/web-api/concepts/apps
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri = "http://localhost:8080";
    private HttpServer server;
    private final CompletableFuture<JsonObject> accessTokenFuture;


    public SpotifyAuthorization(SpotifyView view, String clientId, String clientSecret) {
        this.view = view;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessTokenFuture = new CompletableFuture<>();
    }

    public String getAuthLink() {
        return "https://accounts.spotify.com/authorize?client_id=" + clientId +
                "&redirect_uri=http://localhost:8080&response_type=code";
    }

    public JsonObject authorize() throws IOException, InterruptedException {
        view.showMessage("use this link to request the access code:" + "\n" + getAuthLink());
        startServer();
        return accessTokenFuture.join();
    }

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new SpotifyAuthHandler());
        server.start();
        view.showMessage("waiting for code...");
    }

    private void stopServer() {
        if (server != null) {
            server.stop(1);
        }
    }

    private class SpotifyAuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String response;
            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1];
                view.showMessage("""
                        code received
                        making http request for access_token...
                        """);
                response = "Got the code. Return back to your program.";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
                stopServer();
                exchangeAccessToken(code);
            } else {
                response = "Authorization code not found. Try again.";
                exchange.sendResponseHeaders(400, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            }
        }

        private void exchangeAccessToken(String code) {
            try {
                HttpClient client = HttpClient.newBuilder().build();
                String body = "grant_type=authorization_code&code=" + code +
                        "&redirect_uri=" + redirectUri +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(serverPath + "/api/token"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                accessTokenFuture.complete(jsonResponse);
            } catch (Exception e) {
                accessTokenFuture.completeExceptionally(e);
            }
        }
    }
}
