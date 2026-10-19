package com.tacz.guns.client.resource;

import cn.sh1rocu.tacz.TaCZFabric;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tacz.guns.api.client.animation.gltf.AnimationStructure;
import com.tacz.guns.api.vmlib.LuaAnimationConstant;
import com.tacz.guns.api.vmlib.LuaGunAnimationConstant;
import com.tacz.guns.api.vmlib.LuaLibrary;
import com.tacz.guns.client.resource.manager.DisplayManager;
import com.tacz.guns.client.resource.manager.GltfManager;
import com.tacz.guns.client.resource.manager.PackInfoManager;
import com.tacz.guns.client.resource.pojo.CommonTransformObject;
import com.tacz.guns.client.resource.pojo.PackInfo;
import com.tacz.guns.client.resource.pojo.animation.bedrock.AnimationKeyframes;
import com.tacz.guns.client.resource.pojo.animation.bedrock.BedrockAnimationFile;
import com.tacz.guns.client.resource.pojo.animation.bedrock.SoundEffectKeyframes;
import com.tacz.guns.client.resource.pojo.display.ammo.AmmoDisplay;
import com.tacz.guns.client.resource.pojo.display.attachment.AttachmentDisplay;
import com.tacz.guns.client.resource.pojo.display.block.BlockDisplay;
import com.tacz.guns.client.resource.pojo.display.gun.GunDisplay;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.CubesItem;
import com.tacz.guns.client.resource.serialize.AnimationKeyframesSerializer;
import com.tacz.guns.client.resource.serialize.ItemStackSerializer;
import com.tacz.guns.client.resource.serialize.SoundEffectKeyframesSerializer;
import com.tacz.guns.client.resource.serialize.Vector3fSerializer;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.manager.LazyJsonDataManager;
import com.tacz.guns.resource.manager.ScriptManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;


import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.luaj.vm2.LuaTable;

@Environment(EnvType.CLIENT)
public enum ClientAssetsManager {
   INSTANCE;

   public static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(Identifier.class, new com.tacz.guns.resource.serialize.IdentifierJsonSerializer())
      .registerTypeAdapter(CubesItem.class, new CubesItem.Deserializer())
      .registerTypeAdapter(Vector3f.class, new Vector3fSerializer())
      .registerTypeAdapter(CommonTransformObject.class, new CommonTransformObject.Serializer())
      .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
      .registerTypeAdapter(AnimationKeyframes.class, new AnimationKeyframesSerializer())
      .registerTypeAdapter(SoundEffectKeyframes.class, new SoundEffectKeyframesSerializer())
      .registerTypeAdapter(ItemTransforms.class, newDeserializer("net.minecraft.client.resources.model.cuboid.ItemTransforms$Deserializer"))
      .registerTypeAdapter(ItemTransform.class, newDeserializer("net.minecraft.client.resources.model.cuboid.ItemTransform$Deserializer"))
      .create();
   private static Object newDeserializer(String className) {
      try {
         java.lang.reflect.Constructor<?> constructor = Class.forName(className).getDeclaredConstructor();
         constructor.setAccessible(true);
         return constructor.newInstance();
      } catch (ReflectiveOperationException e) {
         throw new IllegalStateException(e);
      }
   }

   private DisplayManager<GunDisplay> gunDisplay;
   private DisplayManager<AmmoDisplay> ammoDisplay;
   private DisplayManager<AttachmentDisplay> attachmentDisplay;
   private DisplayManager<BlockDisplay> blockDisplay;
   private LazyJsonDataManager<BedrockModelPOJO> bedrockModel;
   private LazyJsonDataManager<BedrockAnimationFile> bedrockAnimation;
   private GltfManager gltfAnimation;
   private final List<LuaLibrary> libList = List.of(new LuaAnimationConstant(), new LuaGunAnimationConstant());
   private ScriptManager scriptManager;
   private PackInfoManager packInfo;
   private List<IdentifiableResourceReloadListener> listeners;

   public void reloadAndRegister(Consumer<IdentifiableResourceReloadListener> register) {
      if (this.listeners == null) {
         this.listeners = new ArrayList<>();
         this.gunDisplay = this.register(new DisplayManager<>(GunDisplay.class, GSON, "display/guns", "GunDisplayLoader"));
         this.ammoDisplay = this.register(new DisplayManager<>(AmmoDisplay.class, GSON, "display/ammo", "AmmoDisplayLoader"));
         this.attachmentDisplay = this.register(new DisplayManager<>(AttachmentDisplay.class, GSON, "display/attachments", "AttachmentDisplayLoader"));
         this.blockDisplay = this.register(new DisplayManager<>(BlockDisplay.class, GSON, "display/blocks", "BlockDisplayLoader"));
         this.bedrockModel = this.register(
            new LazyJsonDataManager<>(BedrockModelPOJO.class, GSON, "geo_models", "BedrockModelLoader", id -> "tacz".equals(id.getNamespace()))
         );
         this.bedrockAnimation = this.register(
            new LazyJsonDataManager<>(
               BedrockAnimationFile.class,
               GSON,
               new FileToIdConverter("animations", ".animation.json"),
               "BedrockAnimationLoader",
               id -> "tacz".equals(id.getNamespace())
            )
         );
         this.gltfAnimation = this.register(new GltfManager());
         this.scriptManager = this.register(new ScriptManager(new FileToIdConverter("scripts", ".lua"), this.libList));
         this.packInfo = this.register(new PackInfoManager());
         this.register(
            new IdentifiableResourceReloadListener() {
               static final Identifier ID = Identifier.fromNamespaceAndPath("tacz", "client_index_manager_reload");

               public Identifier getFabricId() {
                  return ID;
               }

               public CompletableFuture<Void> reload(
                  SharedState sharedState, Executor backgroundExecutor, PreparationBarrier barrier, Executor gameExecutor
               ) {
                  return barrier.wait((Void)null).thenRunAsync(ClientIndexManager::reload, gameExecutor);
               }
            }
         );
      }

      this.listeners.forEach(register);
   }

   private <T extends IdentifiableResourceReloadListener> T register(T listener) {
      this.listeners.add(listener);
      return listener;
   }

   @Nullable
   public GunDisplay getGunDisplay(Identifier id) {
      return this.gunDisplay.getData(id);
   }

   public Set<Entry<Identifier, GunDisplay>> getGunDisplays() {
      return this.gunDisplay.getAllData().entrySet();
   }

   public Set<Identifier> getGunDisplayIds() {
      return this.gunDisplay.getAllData().keySet();
   }

   @Nullable
   public AttachmentDisplay getAttachmentDisplay(Identifier id) {
      return this.attachmentDisplay.getData(id);
   }

   @Nullable
   public AmmoDisplay getAmmoDisplay(Identifier id) {
      return this.ammoDisplay.getData(id);
   }

   @Nullable
   public BlockDisplay getBlockDisplay(Identifier id) {
      return this.blockDisplay.getData(id);
   }

   @Nullable
   public BedrockModelPOJO getBedrockModelPOJO(Identifier id) {
      return this.bedrockModel.getData(id);
   }

   @Nullable
   public BedrockAnimationFile getBedrockAnimations(Identifier id) {
      return this.bedrockAnimation.getData(id);
   }

   @Nullable
   public LuaTable getScript(Identifier id) {
      return this.scriptManager.getScript(id);
   }

   @Nullable
   public AnimationStructure getGltfAnimation(Identifier id) {
      return this.gltfAnimation.getGltfAnimation(id);
   }

   @Nullable
   public PackInfo getPackInfo(String namespace) {
      return this.packInfo.getData(namespace);
   }

   @Nullable
   public PackInfo getPackInfo(@Nullable Identifier namespace) {
      return namespace == null ? null : this.packInfo.getData(namespace.getNamespace());
   }

   @Environment(EnvType.CLIENT)
   public static void reloadAllPack() {
      try {
         Minecraft.getInstance().reloadResourcePacks().get();
         if (TaCZFabric.getServer() != null) {
            CommonAssetsManager.reloadAllPack();
         }
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
}
