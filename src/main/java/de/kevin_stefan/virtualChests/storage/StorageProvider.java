package de.kevin_stefan.virtualChests.storage;

import de.kevin_stefan.virtualChests.VirtualChests;
import de.kevin_stefan.virtualChests.storage.model.VirtualChest;
import de.kevin_stefan.virtualChests.storage.model.VirtualChestHistory;
import dev.dejvokep.boostedyaml.YamlDocument;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
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
        dbConfig.addAnnotatedClass(VirtualChestHistory.class);

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

    public @Nullable VirtualChest getVChest(UUID player, int number) {
        try (EntityManager manager = factory.createEntityManager()) {
            TypedQuery<VirtualChest> query = manager.createNamedQuery("VirtualChest.get", VirtualChest.class);
            query.setParameter("player", player);
            query.setParameter("number", number);
            return query.getSingleResultOrNull();
        }
    }

    public @Nullable VirtualChest setVChest(VirtualChest vChest) {
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

    public @Nullable VirtualChestHistory getLastVChestHistory(UUID player, int number) {
        try (EntityManager manager = factory.createEntityManager()) {
            TypedQuery<VirtualChestHistory> query = manager.createNamedQuery("VirtualChestHistory.get", VirtualChestHistory.class);
            query.setParameter("player", player);
            query.setParameter("number", number);
            query.setMaxResults(1);
            return query.getSingleResultOrNull();
        }
    }

    public List<VirtualChestHistory> getVChestHistory(UUID player, int number) {
        try (EntityManager manager = factory.createEntityManager()) {
            TypedQuery<VirtualChestHistory> query = manager.createNamedQuery("VirtualChestHistory.get", VirtualChestHistory.class);
            query.setParameter("player", player);
            query.setParameter("number", number);
            query.setMaxResults(VirtualChests.getPluginConfig().getInt("keep_last"));
            return query.getResultList();
        }
    }

    public @Nullable VirtualChestHistory getVChestHistory(int id, UUID player, int number) {
        try (EntityManager manager = factory.createEntityManager()) {
            TypedQuery<VirtualChestHistory> query = manager.createNamedQuery("VirtualChestHistory.getOne", VirtualChestHistory.class);
            query.setParameter("id", id);
            query.setParameter("player", player);
            query.setParameter("number", number);
            query.setMaxResults(1);
            return query.getSingleResultOrNull();
        }
    }

    public void addVChestHistory(VirtualChestHistory vChestHistory) {
        try (EntityManager manager = factory.createEntityManager()) {
            manager.getTransaction().begin();

            manager.persist(vChestHistory);

            Query deleteQuery = manager.createQuery("delete from VirtualChestHistory where player = :player and number = :number and id not in (select id from VirtualChestHistory where player = :player and number = :number order by timestamp desc limit :limit)");
            deleteQuery.setParameter("player", vChestHistory.getPlayer());
            deleteQuery.setParameter("number", vChestHistory.getNumber());
            deleteQuery.setParameter("limit", VirtualChests.getPluginConfig().getInt("keep_last"));
            deleteQuery.executeUpdate();

            manager.getTransaction().commit();
        }
    }

    public void deleteVChestHistory(VirtualChestHistory vChestHistory) {
        try (EntityManager manager = factory.createEntityManager()) {
            manager.getTransaction().begin();
            manager.remove(vChestHistory);
            manager.getTransaction().commit();
        }
    }

}
