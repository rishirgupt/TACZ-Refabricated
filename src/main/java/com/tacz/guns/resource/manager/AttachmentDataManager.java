package com.tacz.guns.resource.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.network.DataType;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;

public class AttachmentDataManager extends CommonDataManager<AttachmentData> {
   public AttachmentDataManager() {
      super(DataType.ATTACHMENT_DATA, AttachmentData.class, CommonAssetsManager.GSON, "data/attachments", "AttachmentDataLoader");
   }

   protected AttachmentData parseJson(JsonElement element) {
      AttachmentData data = (AttachmentData)this.getGson().fromJson(element, this.getDataClass());
      if (data != null) {
         AttachmentPropertyManager.getModifiers().forEach((key, value) -> {
            String json = this.getGson().toJson(element);
            if (element.isJsonObject()) {
               JsonObject jsonObject = element.getAsJsonObject();
               if (jsonObject.has(key)) {
                  JsonProperty<?> property = value.readJson(json);
                  property.initComponents();
                  data.addModifier(key, property);
               } else if (jsonObject.has(value.getOptionalFields())) {
                  JsonProperty<?> property = value.readJson(json);
                  property.initComponents();
                  data.addModifier(key, property);
               }
            }
         });
      }

      return data;
   }
}
