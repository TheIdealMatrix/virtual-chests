package de.kevin_stefan.virtualChests.storage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "virtual_chests", uniqueConstraints = {@UniqueConstraint(columnNames = {"player", "number"})})
@NamedQuery(name = "VirtualChest.get", query = "from VirtualChest where player = :player and number = :number")
public class VirtualChest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player", nullable = false, length = 16)
    private UUID player;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "content", nullable = false, columnDefinition = "BLOB")
    private byte[] content;

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

    @Override
    public String toString() {
        return "VirtualChest{" + "id=" + id + ", player=" + player + ", number=" + number + ", content=" + Arrays.toString(content) + '}';
    }

}
