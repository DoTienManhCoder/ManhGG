package com.manhgg.rooms.room;

import java.time.Instant;

public class MediaRef {
  private String id;
  private String name;
  private String type;
  private long size;
  private int sortOrder;
  private Instant createdAt;

  public MediaRef() {}

  public MediaRef(String id, String name, String type, long size, int sortOrder, Instant createdAt) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.size = size;
    this.sortOrder = sortOrder;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
