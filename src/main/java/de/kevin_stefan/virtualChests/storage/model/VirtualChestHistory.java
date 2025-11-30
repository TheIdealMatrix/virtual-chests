package de.kevin_stefan.virtualChests.storage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "virtual_chests_history")
@NamedQuery(name = "VirtualChestHistory.get", query = "from VirtualChestHistory where player = :player and number = :number order by timestamp desc")
@NamedQuery(name = "VirtualChestHistory.getOne", query = "from VirtualChestHistory where id = :id and player = :player and number = :number")
public class VirtualChestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player", nullable = false, length = 16)
    private UUID player;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "content", nullable = false, columnDefinition = "BLOB")
    private byte[] content;

    @Column(name = "timestamp", nullable = false)
    protected long timestamp;

    public Long getId() {
        return id;
    }

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "VirtualChestHistory{" +
                "id=" + id +
                ", player=" + player +
                ", number=" + number +
                ", content=" + Arrays.toString(content) +
                ", timestamp=" + timestamp +
                '}';
    }
}
