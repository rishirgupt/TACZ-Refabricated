package com.tacz.guns.resource.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.ICommonResourceProvider;
import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.index.CommonBlockIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource.pojo.data.block.BlockData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AllowAttachmentTagMatcher;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;

public enum CommonNetworkCache implements ICommonResourceProvider {
   INSTANCE;

   public Map<Identifier, GunData> gunData = new HashMap<>();
   public Map<Identifier, AttachmentData> attachmentData = new HashMap<>();
   public Map<Identifier, RecipeFilter> recipeFilter = new HashMap<>();
   public Map<Identifier, BlockData> blockData = new HashMap<>();
   public Map<Identifier, CommonGunIndex> gunIndex = new HashMap<>();
   public Map<Identifier, CommonAmmoIndex> ammoIndex = new HashMap<>();
   public Map<Identifier, CommonAttachmentIndex> attachmentIndex = new HashMap<>();
   public Map<Identifier, CommonBlockIndex> blockIndex = new HashMap<>();
   public Map<Identifier, Set<String>> attachmentTags = new HashMap<>();
   public Map<Identifier, Set<String>> allowAttachmentTags = new HashMap<>();

   @Nullable
   @Override
   public GunData getGunData(Identifier id) {
      return this.gunData.get(id);
   }

   @Nullable
   @Override
   public AttachmentData getAttachmentData(Identifier attachmentId) {
      return this.attachmentData.get(attachmentId);
   }

   @Nullable
   @Override
   public BlockData getBlockData(Identifier id) {
      return this.blockData.get(id);
   }

   @Nullable
   @Override
   public RecipeFilter getRecipeFilter(Identifier id) {
      return this.recipeFilter.get(id);
   }

   @Nullable
   @Override
   public CommonGunIndex getGunIndex(Identifier id) {
      return this.gunIndex.get(id);
   }

   @Nullable
   @Override
   public CommonAmmoIndex getAmmoIndex(Identifier ammoId) {
      return this.ammoIndex.get(ammoId);
   }

   @Nullable
   @Override
   public CommonAttachmentIndex getAttachmentIndex(Identifier attachmentId) {
      return this.attachmentIndex.get(attachmentId);
   }

   @Nullable
   @Override
   public CommonBlockIndex getBlockIndex(Identifier blockId) {
      return this.blockIndex.get(blockId);
   }

   @Nullable
   @Override
   public LuaTable getScript(Identifier scriptId) {
      return null;
   }

   @Override
   public Set<Entry<Identifier, CommonGunIndex>> getAllGuns() {
      return this.gunIndex.entrySet();
   }

   @Override
   public Set<Entry<Identifier, CommonAmmoIndex>> getAllAmmos() {
      return this.ammoIndex.entrySet();
   }

   @Override
   public Set<Entry<Identifier, CommonAttachmentIndex>> getAllAttachments() {
      return this.attachmentIndex.entrySet();
   }

   @Override
   public Set<Entry<Identifier, CommonBlockIndex>> getAllBlocks() {
      return this.blockIndex.entrySet();
   }

   @Override
   public Set<String> getAttachmentTags(Identifier registryName) {
      return this.attachmentTags.get(registryName);
   }

   @Override
   public Set<String> getAllowAttachmentTags(Identifier registryName) {
      return this.allowAttachmentTags.get(registryName);
   }

   public void clear() {
      this.gunData.clear();
      this.attachmentData.clear();
      this.gunIndex.clear();
      this.ammoIndex.clear();
      this.attachmentIndex.clear();
      this.blockIndex.clear();
      this.recipeFilter.clear();
      this.blockData.clear();
      this.attachmentTags.clear();
      this.allowAttachmentTags.clear();
      AllowAttachmentTagMatcher.resetCache();
   }

   public void fromNetwork(Map<DataType, Map<Identifier, String>> cache) {
      this.clear();
      Map<DataType, Map<Identifier, String>> delayed = new HashMap<>();

      for (Entry<DataType, Map<Identifier, String>> entry : cache.entrySet()) {
         switch ((DataType)entry.getKey()) {
            case GUN_INDEX:
            case AMMO_INDEX:
            case ATTACHMENT_INDEX:
            case BLOCK_INDEX:
               delayed.put(entry.getKey(), entry.getValue());
               break;
            default:
               this.fromNetwork(entry.getKey(), entry.getValue());
         }
      }

      for (Entry<DataType, Map<Identifier, String>> entry : delayed.entrySet()) {
         this.fromNetwork(entry.getKey(), entry.getValue());
      }
   }

   private <T> T parse(String json, Class<T> dataClass) {
      return (T)CommonAssetsManager.GSON.fromJson(json, dataClass);
   }

   private AttachmentData parseAttachmentData(String json) {
      AttachmentData data = (AttachmentData)CommonAssetsManager.GSON.fromJson(json, AttachmentData.class);
      JsonElement element = (JsonElement)CommonAssetsManager.GSON.fromJson(json, JsonElement.class);
      if (data != null) {
         AttachmentPropertyManager.getModifiers().forEach((key, value) -> {
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

   private void resolveAttachmentTags(Map<Identifier, String> data) {
      for (Entry<Identifier, String> entry : data.entrySet()) {
         List<String> tags = (List<String>)CommonAssetsManager.GSON.fromJson(entry.getValue(), new TypeToken<List<String>>() {});
         if (entry.getKey().getPath().startsWith("allow_attachments/") && entry.getKey().getPath().length() > 18) {
            Identifier gunId = entry.getKey().withPath(entry.getKey().getPath().substring(18));
            this.allowAttachmentTags.computeIfAbsent(gunId, v -> new HashSet<>()).addAll(tags);
         } else {
            this.attachmentTags.computeIfAbsent(entry.getKey(), v -> new HashSet<>()).addAll(tags);
         }
      }
   }

   private void fromNetwork(DataType type, Map<Identifier, String> data) {
      for (Entry<Identifier, String> entry : data.entrySet()) {
         try {
            switch (type) {
               case GUN_INDEX:
                  this.gunIndex.put(entry.getKey(), this.parse(entry.getValue(), CommonGunIndex.class));
                  break;
               case AMMO_INDEX:
                  this.ammoIndex.put(entry.getKey(), this.parse(entry.getValue(), CommonAmmoIndex.class));
                  break;
               case ATTACHMENT_INDEX:
                  this.attachmentIndex.put(entry.getKey(), this.parse(entry.getValue(), CommonAttachmentIndex.class));
                  break;
               case BLOCK_INDEX:
                  this.blockIndex.put(entry.getKey(), this.parse(entry.getValue(), CommonBlockIndex.class));
                  break;
               case GUN_DATA:
                  this.gunData.put(entry.getKey(), this.parse(entry.getValue(), GunData.class));
                  break;
               case ATTACHMENT_DATA:
                  this.attachmentData.put(entry.getKey(), this.parseAttachmentData(entry.getValue()));
                  break;
               case ATTACHMENT_TAGS:
                  this.resolveAttachmentTags(data);
                  break;
               case RECIPE_FILTER:
                  this.recipeFilter.put(entry.getKey(), this.parse(entry.getValue(), RecipeFilter.class));
                  break;
               case BLOCK_DATA:
                  this.blockData.put(entry.getKey(), this.parse(entry.getValue(), BlockData.class));
            }
         } catch (IllegalArgumentException | JsonParseException exception) {
            GunMod.LOGGER.warn("Failed to parse data from network for {} with id {}", new Object[]{type, entry.getKey(), exception});
         }
      }
   }
}
