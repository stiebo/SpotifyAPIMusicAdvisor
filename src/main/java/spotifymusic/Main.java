package spotifymusic;

import spotifymusic.controller.SpotifyController;
import spotifymusic.model.SpotifyModel;
import spotifymusic.view.SpotifyView;

public class Main {

    private static String getClientId(String[] args) {
        if ((args.length >= 2) && (args[0].equals("-clientid"))) {
            return args[1];
        }
        else {
            throw new RuntimeException("Parameter -clientid missing!");
        }
    }

    private static String getClientSecret(String[] args) {
        if ((args.length >= 4) && (args[2].equals("-clientsecret"))) {
            return args[3];
        }
        else {
            throw new RuntimeException("Parameter -clientsecret missing!");
        }
    }

    private static int getPageSize(String[] args) {

        if ((args.length >= 6) && (args[4].equals("-page"))) {
            try {
                int limit = Integer.parseInt(args[5]);
                if (limit > 0) {
                    return limit;
                }
            }
            catch (NumberFormatException e) { // use default
                }
        }
        return 5;
    }

    public static void main(String[] args) {
        new SpotifyController(
                new SpotifyModel(getPageSize(args)),
                new SpotifyView(),
                getClientId(args),
                getClientSecret(args))
                .run();
    }
}
