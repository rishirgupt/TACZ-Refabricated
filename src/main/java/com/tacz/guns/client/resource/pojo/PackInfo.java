package com.tacz.guns.client.resource.pojo;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PackInfo {
   @SerializedName("version")
   private String version = "1.0.0";
   @SerializedName("name")
   private String name = "custom.tacz.error.no_name";
   @SerializedName("desc")
   private String description = "";
   @SerializedName("license")
   private String license = "All Rights Reserved";
   @SerializedName("authors")
   private List<String> authors = Lists.newArrayList();
   @SerializedName("date")
   private String date = "1919-08-10";
   @SerializedName("url")
   private String url;

   public String getVersion() {
      return this.version;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getLicense() {
      return this.license;
   }

   public List<String> getAuthors() {
      return this.authors;
   }

   public String getDate() {
      return this.date;
   }

   public String getUrl() {
      return this.url;
   }
}
