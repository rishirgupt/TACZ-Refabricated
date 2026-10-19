package com.tacz.guns.client.resource.pojo.model;

public enum BedrockVersion {
   LEGACY("1.10.0"),
   NEW("1.12.0");

   private final String version;

   BedrockVersion(String version) {
      this.version = version;
   }

   public String getVersion() {
      return this.version;
   }

   public static boolean isNewVersion(BedrockModelPOJO bedrockModel) {
      String[] checkVersion = bedrockModel.getFormatVersion().split("\\.", 3);
      String[] newVersion = NEW.getVersion().split("\\.", 3);
      return checkVersion.length == 3 && newVersion.length == 3 ? Integer.parseInt(checkVersion[1]) >= Integer.parseInt(newVersion[1]) : false;
   }

   public static boolean isLegacyVersion(BedrockModelPOJO bedrockModel) {
      return bedrockModel.getFormatVersion().equals(LEGACY.getVersion());
   }
}
