package com.tacz.guns.network.message;

import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.client.resource.ClientRecipeCache;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/** Recipes are no longer synced to clients by vanilla, so the gun smith table recipes travel in their own packet. */
public record ServerMessageSyncRecipes(Map<Identifier, GunSmithTableRecipe> recipes) implements CustomPacketPayload {
   public static final Type<ServerMessageSyncRecipes> TYPE = new Type<>(Identifier.fromNamespaceAndPath("tacz", "sync_recipes"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageSyncRecipes> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.map(LinkedHashMap::new, Identifier.STREAM_CODEC, GunSmithTableRecipe.STREAM_CODEC),
      ServerMessageSyncRecipes::recipes,
      ServerMessageSyncRecipes::new
   );

   @NotNull
   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Environment(EnvType.CLIENT)
   public static void handle(ServerMessageSyncRecipes message, Context context) {
      context.client().execute(() -> ClientRecipeCache.set(message.recipes(), context.client().level));
   }
}
