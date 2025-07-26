package de.kevin_stefan.virtualChests.utils;

import dev.dejvokep.boostedyaml.YamlDocument;

import java.util.List;

public final class PluginLanguage {

    private final YamlDocument config;
    private final String PREFIX;

    public PluginLanguage(YamlDocument config) {
        this.config = config;
        this.PREFIX = config.getString("lang.PREFIX");
    }

    public <T extends ILang> String get(T lang, Object... objects) {
        String message = config.getString("lang." + lang.name());
        message = message.replaceAll("%prefix%", PREFIX);
        for (int i = 0; i < lang.getParams().size(); i++) {
            message = message.replaceAll("%" + lang.getParams().get(i) + "%", String.valueOf(objects[i]));
        }
        return message;
    }

    public interface ILang {
        String name();

        List<String> getParams();
    }

}
