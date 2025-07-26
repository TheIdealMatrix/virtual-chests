package de.kevin_stefan.virtualChests;

import de.kevin_stefan.virtualChests.utils.PluginLanguage;

import java.util.List;

public enum Lang implements PluginLanguage.ILang {
    CHEST_NAME("player", "number"),
    OPEN_CHEST("number"),
    OPEN_CHEST_OTHER("number", "player"),
    NO_CHEST("number");

    private final List<String> params;

    Lang(String... params) {
        this.params = List.of(params);
    }

    public List<String> getParams() {
        return this.params;
    }
}
