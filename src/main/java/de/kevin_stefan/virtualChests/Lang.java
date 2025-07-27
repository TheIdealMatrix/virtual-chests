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

}
