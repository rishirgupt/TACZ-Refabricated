package com.tacz.guns.resource;

import cn.sh1rocu.tacz.TaCZFabric;
import cn.sh1rocu.tacz.api.event.AddReloadListenerEvent;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tacz.guns.api.vmlib.LuaGunLogicConstant;
import com.tacz.guns.api.vmlib.LuaLibrary;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.crafting.result.GunSmithTableResult;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageSyncGunPack;
import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.index.CommonBlockIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.manager.AttachmentDataManager;
import com.tacz.guns.resource.manager.AttachmentsTagManager;
import com.tacz.guns.resource.manager.CommonDataManager;
import com.tacz.guns.resource.manager.INetworkCacheReloadListener;
import com.tacz.guns.resource.manager.LootInjectionManager;
import com.tacz.guns.resource.manager.RecipeFilterManager;
import com.tacz.guns.resource.manager.ScriptManager;
import com.tacz.guns.resource.network.CommonNetworkCache;
import com.tacz.guns.resource.network.DataType;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource.pojo.data.block.BlockData;
import com.tacz.guns.resource.pojo.data.block.TabConfig;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.Ignite;
import com.tacz.guns.resource.pojo.data.loot.LootTableInjection;
import com.tacz.guns.resource.serialize.CommonAmmoIndexSerializer;
import com.tacz.guns.resource.serialize.CommonAttachmentIndexSerializer;
import com.tacz.guns.resource.serialize.CommonBlockIndexSerializer;
import com.tacz.guns.resource.serialize.CommonGunIndexSerializer;
import com.tacz.guns.resource.serialize.DistanceDamagePairSerializer;
import com.tacz.guns.resource.serialize.GunSmithTableIngredientSerializer;
import com.tacz.guns.resource.serialize.GunSmithTableResultSerializer;
import com.tacz.guns.resource.serialize.IgniteSerializer;
import com.tacz.guns.resource.serialize.PairSerializer;
import com.tacz.guns.resource.serialize.Vec3Serializer;
import com.tacz.guns.util.AllowAttachmentTagMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;

public class CommonAssetsManager implements ICommonResourceProvider {
   private static CommonAssetsManager INSTANCE;
   public static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(Identifier.class, new com.tacz.guns.resource.serialize.IdentifierJsonSerializer())
      .registerTypeAdapter(Pair.class, new PairSerializer())
      .registerTypeAdapter(GunSmithTableIngredient.class, new GunSmithTableIngredientSerializer())
      .registerTypeAdapter(GunSmithTableResult.class, new GunSmithTableResultSerializer())
      .registerTypeAdapter(ExtraDamage.DistanceDamagePair.class, new DistanceDamagePairSerializer())
      .registerTypeAdapter(Vec3.class, new Vec3Serializer())
      .registerTypeAdapter(Ignite.class, new IgniteSerializer())
      .registerTypeAdapter(RecipeFilter.class, new RecipeFilter.Deserializer())
      .registerTypeAdapter(CommonGunIndex.class, new CommonGunIndexSerializer())
      .registerTypeAdapter(CommonAmmoIndex.class, new CommonAmmoIndexSerializer())
      .registerTypeAdapter(CommonAttachmentIndex.class, new CommonAttachmentIndexSerializer())
      .registerTypeAdapter(CommonBlockIndex.class, new CommonBlockIndexSerializer())
      .registerTypeAdapter(TabConfig.class, new TabConfig.Deserializer())
      .create();
   private final List<INetworkCacheReloadListener> listeners = new ArrayList<>();
   private CommonDataManager<GunData> gunData;
   private CommonDataManager<AttachmentData> attachmentData;
   private CommonDataManager<BlockData> blockData;
   private CommonDataManager<CommonAmmoIndex> ammoIndex;
   private CommonDataManager<CommonGunIndex> gunIndex;
   private CommonDataManager<CommonAttachmentIndex> attachmentIndex;
   private CommonDataManager<CommonBlockIndex> blockIndex;
   private RecipeFilterManager recipeFilterManager;
   private LootInjectionManager lootInjectionManager;
   private AttachmentsTagManager attachmentsTagManager;
   List<LuaLibrary> libList = List.of(new LuaGunLogicConstant());
   private final ScriptManager scriptManager = new ScriptManager(new FileToIdConverter("scripts", ".lua"), this.libList);
   public RecipeManager recipeManager;

   public void reloadAndRegister(Consumer<PreparableReloadListener> register) {
      this.gunData = this.register(new CommonDataManager<>(DataType.GUN_DATA, GunData.class, GSON, "data/guns", "GunDataLoader"));
      this.attachmentData = this.register(new AttachmentDataManager());
      this.attachmentsTagManager = this.register(new AttachmentsTagManager());
      this.recipeFilterManager = this.register(new RecipeFilterManager());
      this.lootInjectionManager = new LootInjectionManager();
      register.accept(this.lootInjectionManager);
      this.blockData = this.register(new CommonDataManager<>(DataType.BLOCK_DATA, BlockData.class, GSON, "data/blocks", "BlockDataLoader"));
      register.accept(this.scriptManager);
      this.ammoIndex = this.register(new CommonDataManager<>(DataType.AMMO_INDEX, CommonAmmoIndex.class, GSON, "index/ammo", "AmmoIndexLoader"));
      this.gunIndex = this.register(new CommonDataManager<>(DataType.GUN_INDEX, CommonGunIndex.class, GSON, "index/guns", "GunIndexLoader"));
      this.attachmentIndex = this.register(
         new CommonDataManager<>(DataType.ATTACHMENT_INDEX, CommonAttachmentIndex.class, GSON, "index/attachments", "AttachmentIndexLoader")
      );
      this.blockIndex = this.register(new CommonDataManager<>(DataType.BLOCK_INDEX, CommonBlockIndex.class, GSON, "index/blocks", "BlockIndexLoader"));
      this.listeners.forEach(register);
      register.accept(
         (sharedState, backgroundExecutor, barrier, gameExecutor) -> barrier.wait((Void)null)
            .thenRunAsync(AllowAttachmentTagMatcher::resetCache, gameExecutor)
      );
   }

   private <T extends INetworkCacheReloadListener> T register(T listener) {
      this.listeners.add(listener);
      return listener;
   }

   public Map<DataType, Map<Identifier, String>> getNetworkCache() {
      Builder<DataType, Map<Identifier, String>> builder = ImmutableMap.builder();

      for (INetworkCacheReloadListener listener : this.listeners) {
         builder.put(listener.getType(), listener.getNetworkCache());
      }

      return builder.build();
   }

   @Nullable
   @Override
   public GunData getGunData(Identifier id) {
      return this.gunData.getData(id);
   }

   @Nullable
   @Override
   public AttachmentData getAttachmentData(Identifier id) {
      return this.attachmentData.getData(id);
   }

   @Nullable
   @Override
   public BlockData getBlockData(Identifier id) {
      return this.blockData.getData(id);
   }

   @Nullable
   @Override
   public RecipeFilter getRecipeFilter(Identifier id) {
      return this.recipeFilterManager.getFilter(id);
   }

   public List<LootTableInjection> getLootTableInjections(Identifier lootTable) {
      return this.lootInjectionManager == null ? List.of() : this.lootInjectionManager.getInjections(lootTable);
   }

   @Nullable
   @Override
   public CommonGunIndex getGunIndex(Identifier gunId) {
      return this.gunIndex.getData(gunId);
   }

   @Override
   public Set<Entry<Identifier, CommonGunIndex>> getAllGuns() {
      return this.gunIndex.getAllData().entrySet();
   }

   @Nullable
   @Override
   public CommonAmmoIndex getAmmoIndex(Identifier ammoId) {
      return this.ammoIndex.getData(ammoId);
   }

   @Override
   public Set<Entry<Identifier, CommonAmmoIndex>> getAllAmmos() {
      return this.ammoIndex.getAllData().entrySet();
   }

   @Nullable
   @Override
   public CommonAttachmentIndex getAttachmentIndex(Identifier attachmentId) {
      return this.attachmentIndex.getData(attachmentId);
   }

   @Override
   public Set<Entry<Identifier, CommonAttachmentIndex>> getAllAttachments() {
      return this.attachmentIndex.getAllData().entrySet();
   }

   @Override
   public LuaTable getScript(Identifier scriptId) {
      return this.scriptManager.getScript(scriptId);
   }

   @Nullable
   @Override
   public CommonBlockIndex getBlockIndex(Identifier blockId) {
      return this.blockIndex.getData(blockId);
   }

   @Override
   public Set<Entry<Identifier, CommonBlockIndex>> getAllBlocks() {
      return this.blockIndex.getAllData().entrySet();
   }

   @Override
   public Set<String> getAttachmentTags(Identifier registryName) {
      return this.attachmentsTagManager.getAttachmentTags(registryName);
   }

   @Override
   public Set<String> getAllowAttachmentTags(Identifier registryName) {
      return this.attachmentsTagManager.getAllowAttachmentTags(registryName);
   }

   @Nullable
   public static CommonAssetsManager getInstance() {
      return INSTANCE;
   }

   public static void clearInstance() {
      INSTANCE = null;
   }

   public static ICommonResourceProvider get() {
      return INSTANCE == null ? CommonNetworkCache.INSTANCE : INSTANCE;
   }

   public static void onReload(AddReloadListenerEvent event) {
      CommonAssetsManager commonAssetsManager = new CommonAssetsManager();
      commonAssetsManager.reloadAndRegister(event::addListener);
      INSTANCE = commonAssetsManager;
      INSTANCE.recipeManager = event.getServerResources().getRecipeManager();
   }

   public static void onReload(RegistryAccess registries, boolean client) {
      if (!client && getInstance() != null && getInstance().recipeManager != null) {
         for (RecipeHolder<GunSmithTableRecipe> recipe : ModRecipe.allRecipes(getInstance().recipeManager)) {
            ((GunSmithTableRecipe)recipe.value()).init(registries);
         }
      }
   }

   public static void onServerStopped(MinecraftServer server) {
      clearInstance();
   }

   public static void OnDatapackSync(ServerPlayer player, boolean joined) {
      if (getInstance() != null) {
         ServerMessageSyncGunPack message = new ServerMessageSyncGunPack(getInstance().getNetworkCache());
         NetworkHandler.sendToClientPlayer(message, player);
      }
   }

   public static void reloadAllPack() {
      MinecraftServer server = TaCZFabric.getServer();
      if (server != null) {
         PackRepository packrepository = server.getPackRepository();
         packrepository.reload();
         Collection<String> collection = packrepository.getSelectedIds();
         server.reloadResources(collection);
      }
   }
}
