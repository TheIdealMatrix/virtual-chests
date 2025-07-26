package de.kevin_stefan.virtualChests.storage;

import de.kevin_stefan.virtualChests.VirtualChests;
import de.kevin_stefan.virtualChests.storage.model.VirtualChest;
import dev.dejvokep.boostedyaml.YamlDocument;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

public class StorageProvider {

    private static StorageProvider instance;
    private final EntityManagerFactory factory;

    private StorageProvider() {
        VirtualChests.getPluginLogger().info("Loading Storage...");

        Configuration dbConfig = new Configuration();
        YamlDocument config = VirtualChests.getPluginConfig();
        String storageType = config.getString("storage.type", "sqlite");
        if (storageType.equalsIgnoreCase("mysql")) {
            VirtualChests.getPluginLogger().info("using MySQL");

            String address = config.getString("storage.mysql.address");
            String database = config.getString("storage.mysql.database");
            String username = config.getString("storage.mysql.username");
            String password = config.getString("storage.mysql.password");

            dbConfig.setJdbcUrl("jdbc:mysql://" + address + "/" + database);
            dbConfig.setCredentials(username, password);
            dbConfig.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            dbConfig.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        } else {
            VirtualChests.getPluginLogger().info("using SQLite");

            dbConfig.setJdbcUrl("jdbc:sqlite:" + new File(VirtualChests.getInstance().getDataFolder(), "data.db"));
            dbConfig.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
            dbConfig.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC");
        }

        dbConfig.setProperty("hibernate.hbm2ddl.auto", "update");
        dbConfig.setProperty("hibernate.c3p0.min_size", 1);
        dbConfig.setProperty("hibernate.c3p0.max_size", 10);
        dbConfig.setProperty("hibernate.show_sql", VirtualChests.getPluginLogger().isDebug());

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        dbConfig.addAnnotatedClass(VirtualChest.class);

        factory = dbConfig.buildSessionFactory();
    }

    public static StorageProvider getInstance() {
        if (instance == null) {
            instance = new StorageProvider();
        }
        return instance;
    }

    public static void close() {
        if (instance != null) {
            instance.factory.close();
        }
    }

    public void testConnection() {
        try (EntityManager manager = factory.createEntityManager()) {
            manager.isOpen();
        }
    }

    @Nullable
    public VirtualChest getVChest(UUID player, int number) {
        try (EntityManager manager = factory.createEntityManager()) {
            TypedQuery<VirtualChest> query = manager.createNamedQuery("VirtualChest.get", VirtualChest.class);
            query.setParameter("player", player);
            query.setParameter("number", number);
            return query.getSingleResultOrNull();
        }
    }

    public VirtualChest setVChest(VirtualChest vChest) {
        try (EntityManager manager = factory.createEntityManager()) {
            manager.getTransaction().begin();
            VirtualChest virtualChest = manager.merge(vChest);
            manager.getTransaction().commit();
            return virtualChest;
        }
    }

    public void deleteVChest(VirtualChest vChest) {
        try (EntityManager manager = factory.createEntityManager()) {
            manager.getTransaction().begin();
            manager.remove(vChest);
            manager.getTransaction().commit();
        }
    }

}
