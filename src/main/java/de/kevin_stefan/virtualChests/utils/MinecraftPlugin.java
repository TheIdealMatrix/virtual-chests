package de.kevin_stefan.virtualChests.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.spigot.SpigotSerializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class MinecraftPlugin extends JavaPlugin {

    private static PluginLogger logger;
    private static YamlDocument config;
    private static PluginLanguage language;

    @Override
    public void onEnable() {
        logger = new PluginLogger(getLogger());
        config = initializeConfig();
        language = new PluginLanguage(config);
    }

    public static PluginLogger getPluginLogger() {
        return logger;
    }

    public static YamlDocument getPluginConfig() {
        return config;
    }

    public static PluginLanguage getPluginLanguage() {
        return language;
    }

    private YamlDocument initializeConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        InputStream defaultConfigStream = getResource("config.yml");

        if (defaultConfigStream == null) {
            throw new RuntimeException("No default config.yml file found inside the plugins resources folder");
        }

        LoaderSettings loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build();
        UpdaterSettings updaterSettings = UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build();
        GeneralSettings generalSettings = GeneralSettings.builder().setSerializer(SpigotSerializer.getInstance()).build();

        try {
            return YamlDocument.create(configFile, defaultConfigStream, loaderSettings, updaterSettings, generalSettings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
