package de.kevin_stefan.virtualChests.storage;

import de.kevin_stefan.virtualChests.VirtualChests;
import de.kevin_stefan.virtualChests.storage.model.VirtualChest;
import de.kevin_stefan.virtualChests.storage.model.VirtualChestHistory;
import dev.dejvokep.boostedyaml.YamlDocument;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
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

    //region VirtualChest
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

    public long getVChestCount(UUID player, @Nullable Integer number) {
        try (EntityManager manager = factory.createEntityManager()) {
            CriteriaBuilder builder = manager.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<VirtualChest> root = query.from(VirtualChest.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("player"), player));
            if (number != null) {
                predicates.add(builder.equal(root.get("number"), number));
            }

            query.select(builder.count(root)).where(builder.and(predicates));
            return manager.createQuery(query).getSingleResult();
        }
    }

    public boolean doesVChestExist(UUID player, int number) {
        return getVChestCount(player, number) > 0;
    }
    //endregion

    //region VirtualChestHistory
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
            return query.getResultList();
        }
    }

    public List<VirtualChestHistory> getVChestHistory(UUID player, int number, int page) {
        int pageSize = VirtualChests.getPluginConfig().getInt("history_page_size");
        int offset = (page - 1) * pageSize;
        try (EntityManager manager = factory.createEntityManager()) {
            TypedQuery<VirtualChestHistory> query = manager.createNamedQuery("VirtualChestHistory.get", VirtualChestHistory.class);
            query.setParameter("player", player);
            query.setParameter("number", number);
            query.setFirstResult(offset);
            query.setMaxResults(pageSize);
            return query.getResultList();
        }
    }

    public long getVChestHistoryCount(UUID player, int number) {
        try (EntityManager manager = factory.createEntityManager()) {
            CriteriaBuilder builder = manager.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<VirtualChestHistory> root = query.from(VirtualChestHistory.class);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("player"), player));
            predicates.add(builder.equal(root.get("number"), number));

            query.select(builder.count(root)).where(builder.and(predicates));

            return manager.createQuery(query).getSingleResult();
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

            int keepLast = VirtualChests.getPluginConfig().getInt("keep_last");
            if (keepLast > 0) {
                Query deleteQuery = manager.createQuery("delete from VirtualChestHistory where player = :player and number = :number and id not in (select id from VirtualChestHistory where player = :player and number = :number order by timestamp desc limit :limit)");
                deleteQuery.setParameter("player", vChestHistory.getPlayer());
                deleteQuery.setParameter("number", vChestHistory.getNumber());
                deleteQuery.setParameter("limit", keepLast);
                deleteQuery.executeUpdate();
            }

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
    //endregion

    //region Transfer
    public int transferChests(UUID player, UUID target, @Nullable Integer number, @Nullable Integer numberTo) {
        if (number == null && numberTo != null) {
            throw new IllegalArgumentException("number cannot be null when numberTo is not null");
        }

        try (EntityManager manager = factory.createEntityManager()) {
            try {
                manager.getTransaction().begin();

                // Delete target players chest & history
                preTransferDelete(manager, target, numberTo);

                // Transfer
                CriteriaBuilder builderChest = manager.getCriteriaBuilder();
                CriteriaUpdate<VirtualChest> updateChest = builderChest.createCriteriaUpdate(VirtualChest.class);
                Root<VirtualChest> rootChest = updateChest.from(VirtualChest.class);

                updateChest.set(rootChest.get("player"), target);
                List<Predicate> predicatesChest = new ArrayList<>();
                predicatesChest.add(builderChest.equal(rootChest.get("player"), player));
                if (number != null) {
                    predicatesChest.add(builderChest.equal(rootChest.get("number"), number));
                    if (numberTo != null) {
                        updateChest.set(rootChest.get("number"), numberTo);
                    }
                }
                updateChest.where(builderChest.and(predicatesChest));
                int modifiedChests = manager.createQuery(updateChest).executeUpdate();

                if (VirtualChests.getPluginConfig().getBoolean("transfer_history")) {
                    CriteriaBuilder builderHistory = manager.getCriteriaBuilder();
                    CriteriaUpdate<VirtualChestHistory> updateHistory = builderHistory.createCriteriaUpdate(VirtualChestHistory.class);
                    Root<VirtualChestHistory> rootHistory = updateHistory.from(VirtualChestHistory.class);

                    updateHistory.set(rootHistory.get("player"), target);
                    List<Predicate> predicatesHistory = new ArrayList<>();
                    predicatesHistory.add(builderHistory.equal(rootHistory.get("player"), player));
                    if (number != null) {
                        predicatesHistory.add(builderHistory.equal(rootHistory.get("number"), number));
                        if (numberTo != null) {
                            updateHistory.set(rootHistory.get("number"), numberTo);
                        }
                    }
                    updateHistory.where(builderHistory.and(predicatesHistory));
                    manager.createQuery(updateHistory).executeUpdate();
                }

                manager.getTransaction().commit();
                return modifiedChests;
            } catch (Exception e) {
                manager.getTransaction().rollback();
                throw e;
            }
        }
    }

    private void preTransferDelete(EntityManager manager, UUID player, @Nullable Integer number) {
        // Chest
        CriteriaBuilder builderChest = manager.getCriteriaBuilder();
        CriteriaDelete<VirtualChest> deleteChest = builderChest.createCriteriaDelete(VirtualChest.class);
        Root<VirtualChest> rootChest = deleteChest.from(VirtualChest.class);

        List<Predicate> predicatesChest = new ArrayList<>();
        predicatesChest.add(builderChest.equal(rootChest.get("player"), player));
        if (number != null) {
            predicatesChest.add(builderChest.equal(rootChest.get("number"), number));
        }
        deleteChest.where(builderChest.and(predicatesChest));
        manager.createQuery(deleteChest).executeUpdate();

        // History
        CriteriaBuilder builderHistory = manager.getCriteriaBuilder();
        CriteriaDelete<VirtualChestHistory> deleteHistory = builderHistory.createCriteriaDelete(VirtualChestHistory.class);
        Root<VirtualChestHistory> rootHistory = deleteHistory.from(VirtualChestHistory.class);

        List<Predicate> predicatesHistory = new ArrayList<>();
        predicatesHistory.add(builderHistory.equal(rootHistory.get("player"), player));
        if (number != null) {
            predicatesHistory.add(builderHistory.equal(rootHistory.get("number"), number));
        }
        deleteHistory.where(builderHistory.and(predicatesHistory));
        manager.createQuery(deleteHistory).executeUpdate();
    }
    //endregion

}
