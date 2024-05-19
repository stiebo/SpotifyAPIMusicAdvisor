package spotifymusic.view;

import spotifymusic.dto.ResponseDto;

public class SpotifyView {

    public void printMenu() {
        System.out.println("""
                Available commands:
                auth
                new
                featured
                categories
                playlists <Category name>
                next
                prev
                help
                exit
                """);
    }

    public void showAuthorizationSuccess(String authorization) {
        System.out.println("response:");
        System.out.println(authorization);
        System.out.println("---SUCCESS---");
    }

    public void showResponse(String message, ResponseDto dto) {
        System.out.println(message);
        dto.getBody().getItems().forEach(System.out::println);
        int curr = dto.getBody().getOffset() / dto.getBody().getLimit() + 1;
        int max = dto.getBody().getTotal() / dto.getBody().getLimit();
        System.out.printf("---PAGE %d OF %d---%n", curr, max);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}
