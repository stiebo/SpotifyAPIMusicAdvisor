package spotifymusic.controller;

import spotifymusic.domain.SpotifyContainer;
import spotifymusic.dto.ResponseDto;
import spotifymusic.model.SpotifyModel;
import spotifymusic.view.SpotifyView;
import com.google.gson.JsonObject;

import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;

public class SpotifyController {
    private final SpotifyModel model;
    private final SpotifyView view;
    private final SpotifyAuthorization auth;

    private boolean keepRunning;
    private final Scanner scanner;
    private String accessToken;
    private boolean authorized;

    private record LastAction(String lastMessage, String lastCommand, Function<String, ResponseDto> lastDataModel,
                              SpotifyContainer lastContainer) {
    }
    private LastAction lastAction;

    public SpotifyController(SpotifyModel model, SpotifyView view, String clientId, String clientSecret) {
        this.model = model;
        this.view = view;
        this.auth = new SpotifyAuthorization(view, clientId, clientSecret);
        this.keepRunning = true;
        this.scanner = new Scanner(System.in);
        this.authorized = false;
        this.lastAction = null;
    }

    public void run() {
        view.printMenu();
        while (keepRunning) {
            System.out.print("> ");
            String input = scanner.nextLine();
            actionInput(input);
        }
    }

    private void actionInput(String input) {
        String[] splitInput = input.split("\\s+");
        switch (splitInput[0]) {
            case "auth" -> handleAuth();
            case "new" -> handleCommand("---NEW RELEASES---", "new",
                    requestPath -> model.getNewReleases(accessToken, requestPath), null);
            case "featured" -> handleCommand("---FEATURED---", "featured",
                    requestPath -> model.getFeaturedPlaylists(accessToken, requestPath), null);
            case "categories" -> handleCommand("---CATEGORIES---", "categories",
                    requestPath -> model.getCategories(accessToken, requestPath,
                            Optional.empty()), null);
            case "playlists" -> {
                try {
                    String catName = input.substring(splitInput[0].length()).trim();
                    handleCommand("---" + catName + " PLAYLISTS---", "playlists",
                            requestPath -> model.getPlaylistsForCategory(catName, accessToken, requestPath),
                            null);
                } catch (ArrayIndexOutOfBoundsException e) {
                    view.showMessage("Missing argument");
                }
            }
            case "next" -> handleNext();
            case "prev" -> handlePrev();
            case "help" -> view.printMenu();
            case "exit" -> exit();
            default -> view.showMessage("Unknown command");
        }
    }

    private void handleAuth() {
        if (!authorized) {
            try {
                JsonObject authResponse = auth.authorize();
                accessToken = authResponse.get("access_token").getAsString();
                authorized = true;
                view.showAuthorizationSuccess(authResponse.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            view.showMessage("code received");
        } else {
            view.showMessage("Already authorized");
        }
    }

    private void handleCommand(String message, String command, Function<String, ResponseDto> dataModel,
                               String requestPath) {
        if (authorized) {
            ResponseDto dto = dataModel.apply(requestPath);
            if (dto.getStatus() == 200) {
                view.showResponse(message, dto);
                lastAction = new LastAction(message, command, dataModel, dto.getBody());
            } else {
                view.showMessage(dto.getMessage());
            }
        } else {
            view.showMessage("Please, provide access for application.");
        }
    }

    private void handleNext() {
        if (lastAction != null) {
            if (lastAction.lastContainer.getNext() != null) {
                handleCommand(lastAction.lastMessage, lastAction.lastCommand,
                        lastAction.lastDataModel, lastAction.lastContainer.getNext());
            } else {
                view.showMessage("No more pages.");
            }
        } else {
            view.showMessage("Wrong input");
        }
    }

    private void handlePrev() {
        if (lastAction != null) {
            if (lastAction.lastContainer.getPrevious() != null) {
                handleCommand(lastAction.lastMessage, lastAction.lastCommand,
                        lastAction.lastDataModel, lastAction.lastContainer.getPrevious());
            } else {
                view.showMessage("No more pages.");
            }
        } else {
            view.showMessage("Wrong input");
        }
    }

    private void exit() {
        keepRunning = false;
        view.showMessage("---GOODBYE!---");
    }

}
