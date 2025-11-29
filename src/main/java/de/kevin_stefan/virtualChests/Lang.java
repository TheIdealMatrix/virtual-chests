package de.kevin_stefan.virtualChests;

public final class Lang {

    public record CHEST_NAME(String player, int number) {
    }

    public record OPEN_CHEST(int number) {
    }

    public record OPEN_CHEST_OTHER(int number, String player) {
    }

    public record NO_CHEST(int number) {
    }

    public record HISTORY_LIST_HEADER(int number, String player) {
    }

    public record HISTORY_LIST_LINE(int number, String player, long id, String date) {
    }

    public record HISTORY_LIST_FOOTER(int number, String player, int page, int max_page, int prev_page, int next_page) {
    }

    public record HISTORY_DATE_FORMAT() {
    }

    public record HISTORY_CHEST_NAME(int number, String player, int id, String date) {
    }

    public record HISTORY_NOT_FOUND(int number, String player) {
    }

    public record HISTORY_NOT_FOUND_PAGE(int number, String player, int page) {
    }

    public record HISTORY_NOT_FOUND_ID(int number, String player, int id) {
    }

    public record HISTORY_RESTORED(int number, String player, int id, String date) {
    }

}
