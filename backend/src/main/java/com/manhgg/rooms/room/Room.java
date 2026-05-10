package com.manhgg.rooms.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("rooms")
public class Room {
  @Id
  private String id;

  @NotBlank
  private String address;

  private String realAddress = "";

  @NotBlank
  private String price;

  @NotBlank
  private String code;

  @Pattern(regexp = "open|lock")
  private String status = "open";

  private String note = "";
  private String area = "";
  private String layout = "";
  private String furniture = "";
  private String amenities = "";
  private String sellingPoints = "";
  private String contact = "";
  private List<MediaRef> media = new ArrayList<>();
  private Instant createdAt = Instant.now();
  private Instant updatedAt = Instant.now();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getRealAddress() {
    return realAddress;
  }

  public void setRealAddress(String realAddress) {
    this.realAddress = realAddress;
  }

  public String getPrice() {
    return price;
  }

  public void setPrice(String price) {
    this.price = price;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getArea() {
    return area;
  }

  public void setArea(String area) {
    this.area = area;
  }

  public String getLayout() {
    return layout;
  }

  public void setLayout(String layout) {
    this.layout = layout;
  }

  public String getFurniture() {
    return furniture;
  }

  public void setFurniture(String furniture) {
    this.furniture = furniture;
  }

  public String getAmenities() {
    return amenities;
  }

  public void setAmenities(String amenities) {
    this.amenities = amenities;
  }

  public String getSellingPoints() {
    return sellingPoints;
  }

  public void setSellingPoints(String sellingPoints) {
    this.sellingPoints = sellingPoints;
  }

  public String getContact() {
    return contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public List<MediaRef> getMedia() {
    return media;
  }

  public void setMedia(List<MediaRef> media) {
    this.media = media;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
