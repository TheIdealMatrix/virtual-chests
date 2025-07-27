package de.kevin_stefan.virtualChests.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.lang.reflect.RecordComponent;

public final class PluginLanguage {

    private final YamlDocument config;
    private final String PREFIX;

    public PluginLanguage(YamlDocument config) {
        this.config = config;
        this.PREFIX = config.getString("lang.PREFIX");
    }

    public <T extends Record> String get(T record) {
        Class<?> clazz = record.getClass();
        String message = config.getString("lang." + clazz.getSimpleName()).replaceAll("%prefix%", PREFIX);
        for (RecordComponent component : clazz.getRecordComponents()) {
            try {
                String name = component.getName();
                Object value = component.getAccessor().invoke(record);
                message = message.replaceAll("%" + name + "%", String.valueOf(value));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return message;
    }

    public <T extends Record> Component getFormatted(T record) {
        return MiniMessage.miniMessage().deserialize(get(record));
    }

}
