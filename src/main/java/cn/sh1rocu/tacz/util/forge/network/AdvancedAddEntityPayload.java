package cn.sh1rocu.tacz.util.forge.network;

import com.tacz.guns.GunMod;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public record AdvancedAddEntityPayload(int entityId, byte[] customPayload) implements CustomPacketPayload {
   public static final Type<AdvancedAddEntityPayload> TYPE = new Type(Identifier.fromNamespaceAndPath("tacz", "advanced_add_entity"));
   public static final StreamCodec<FriendlyByteBuf, AdvancedAddEntityPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      AdvancedAddEntityPayload::entityId,
      ByteBufCodecs.BYTE_ARRAY,
      AdvancedAddEntityPayload::customPayload,
      AdvancedAddEntityPayload::new
   );

   public AdvancedAddEntityPayload(Entity e) {
      this(e.getId(), writeCustomData(e));
   }

   private static byte[] writeCustomData(Entity entity) {
      if (entity instanceof IEntityWithComplexSpawn additionalSpawnData) {
         RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), entity.registryAccess());

         try {
            additionalSpawnData.writeSpawnData(buf);
            return buf.array();
         } finally {
            buf.release();
         }
      } else {
         return new byte[0];
      }
   }

   @Environment(EnvType.CLIENT)
   public static void handle(AdvancedAddEntityPayload message, Context context) {
      context.client().execute(() -> {
         try {
            Entity entity = context.player().level().getEntity(message.entityId());
            if (entity instanceof IEntityWithComplexSpawn entityAdditionalSpawnData) {
               RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(message.customPayload()), entity.registryAccess());

               try {
                  entityAdditionalSpawnData.readSpawnData(buf);
               } finally {
                  buf.release();
               }
            }
         } catch (Throwable t) {
            GunMod.LOGGER.error("Failed to handle advanced add entity from server.", t);
            context.responseSender().disconnect(Component.literal("Failed to send AdvancedAddEntityPayload to client"));
         }
      });
   }

   public Type<AdvancedAddEntityPayload> type() {
      return TYPE;
   }
}
