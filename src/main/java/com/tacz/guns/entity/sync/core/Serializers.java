package com.tacz.guns.entity.sync.core;

import java.util.UUID;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.core.HolderLookup.Provider;

public class Serializers {
   public static final IDataSerializer<Boolean> BOOLEAN = new IDataSerializer<Boolean>() {
      public void write(FriendlyByteBuf buf, Boolean value) {
         buf.writeBoolean(value);
      }

      public Boolean read(FriendlyByteBuf buf) {
         return buf.readBoolean();
      }

      public Tag write(Provider provider, Boolean value) {
         return ByteTag.valueOf(value);
      }

      public Boolean read(Provider provider, Tag tag) {
         return ((ByteTag)tag).byteValue() != 0;
      }
   };
   public static final IDataSerializer<Byte> BYTE = new IDataSerializer<Byte>() {
      public void write(FriendlyByteBuf buf, Byte value) {
         buf.writeByte(value);
      }

      public Byte read(FriendlyByteBuf buf) {
         return buf.readByte();
      }

      public Tag write(Provider provider, Byte value) {
         return ByteTag.valueOf(value);
      }

      public Byte read(Provider provider, Tag tag) {
         return ((ByteTag)tag).byteValue();
      }
   };
   public static final IDataSerializer<Short> SHORT = new IDataSerializer<Short>() {
      public void write(FriendlyByteBuf buf, Short value) {
         buf.writeShort(value);
      }

      public Short read(FriendlyByteBuf buf) {
         return buf.readShort();
      }

      public Tag write(Provider provider, Short value) {
         return ShortTag.valueOf(value);
      }

      public Short read(Provider provider, Tag tag) {
         return ((ShortTag)tag).shortValue();
      }
   };
   public static final IDataSerializer<Integer> INTEGER = new IDataSerializer<Integer>() {
      public void write(FriendlyByteBuf buf, Integer value) {
         buf.writeVarInt(value);
      }

      public Integer read(FriendlyByteBuf buf) {
         return buf.readVarInt();
      }

      public Tag write(Provider provider, Integer value) {
         return IntTag.valueOf(value);
      }

      public Integer read(Provider provider, Tag tag) {
         return ((IntTag)tag).intValue();
      }
   };
   public static final IDataSerializer<Long> LONG = new IDataSerializer<Long>() {
      public void write(FriendlyByteBuf buf, Long value) {
         buf.writeLong(value);
      }

      public Long read(FriendlyByteBuf buf) {
         return buf.readLong();
      }

      public Tag write(Provider provider, Long value) {
         return LongTag.valueOf(value);
      }

      public Long read(Provider provider, Tag tag) {
         return ((LongTag)tag).longValue();
      }
   };
   public static final IDataSerializer<Float> FLOAT = new IDataSerializer<Float>() {
      public void write(FriendlyByteBuf buf, Float value) {
         buf.writeFloat(value);
      }

      public Float read(FriendlyByteBuf buf) {
         return buf.readFloat();
      }

      public Tag write(Provider provider, Float value) {
         return FloatTag.valueOf(value);
      }

      public Float read(Provider provider, Tag tag) {
         return ((FloatTag)tag).floatValue();
      }
   };
   public static final IDataSerializer<Double> DOUBLE = new IDataSerializer<Double>() {
      public void write(FriendlyByteBuf buf, Double value) {
         buf.writeDouble(value);
      }

      public Double read(FriendlyByteBuf buf) {
         return buf.readDouble();
      }

      public Tag write(Provider provider, Double value) {
         return DoubleTag.valueOf(value);
      }

      public Double read(Provider provider, Tag tag) {
         return ((DoubleTag)tag).doubleValue();
      }
   };
   public static final IDataSerializer<Character> CHARACTER = new IDataSerializer<Character>() {
      public void write(FriendlyByteBuf buf, Character value) {
         buf.writeChar(value);
      }

      public Character read(FriendlyByteBuf buf) {
         return buf.readChar();
      }

      public Tag write(Provider provider, Character value) {
         return IntTag.valueOf(value);
      }

      public Character read(Provider provider, Tag tag) {
         return (char)((IntTag)tag).intValue();
      }
   };
   public static final IDataSerializer<String> STRING = new IDataSerializer<String>() {
      public void write(FriendlyByteBuf buf, String value) {
         buf.writeUtf(value);
      }

      public String read(FriendlyByteBuf buf) {
         return buf.readUtf();
      }

      public Tag write(Provider provider, String value) {
         return StringTag.valueOf(value);
      }

      public String read(Provider provider, Tag tag) {
         return tag.asString().orElse("");
      }
   };
   public static final IDataSerializer<CompoundTag> TAG_COMPOUND = new IDataSerializer<CompoundTag>() {
      public void write(FriendlyByteBuf buf, CompoundTag value) {
         buf.writeNbt(value);
      }

      public CompoundTag read(FriendlyByteBuf buf) {
         return buf.readNbt();
      }

      public Tag write(Provider provider, CompoundTag value) {
         return value;
      }

      public CompoundTag read(Provider provider, Tag tag) {
         return (CompoundTag)tag;
      }
   };
   public static final IDataSerializer<BlockPos> BLOCK_POS = new IDataSerializer<BlockPos>() {
      public void write(FriendlyByteBuf buf, BlockPos value) {
         buf.writeBlockPos(value);
      }

      public BlockPos read(FriendlyByteBuf buf) {
         return buf.readBlockPos();
      }

      public Tag write(Provider provider, BlockPos value) {
         return LongTag.valueOf(value.asLong());
      }

      public BlockPos read(Provider provider, Tag tag) {
         return BlockPos.of(((LongTag)tag).longValue());
      }
   };
   public static final IDataSerializer<UUID> UUID = new IDataSerializer<UUID>() {
      public void write(FriendlyByteBuf buf, UUID value) {
         buf.writeUUID(value);
      }

      public UUID read(FriendlyByteBuf buf) {
         return buf.readUUID();
      }

      public Tag write(Provider provider, UUID value) {
         CompoundTag compound = new CompoundTag();
         compound.putLong("Most", value.getMostSignificantBits());
         compound.putLong("Least", value.getLeastSignificantBits());
         return compound;
      }

      public UUID read(Provider provider, Tag tag) {
         CompoundTag compound = (CompoundTag)tag;
         return new UUID(compound.getLongOr("Most", 0L), compound.getLongOr("Least", 0L));
      }
   };
   public static final IDataSerializer<ItemStack> ITEM_STACK = new IDataSerializer<ItemStack>() {
      public void write(FriendlyByteBuf buf, ItemStack value) {
         if (buf instanceof net.minecraft.network.RegistryFriendlyByteBuf rb) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(rb, value);
         } else {
            buf.writeNbt(ItemStack.OPTIONAL_CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, value).getOrThrow());
         }
      }

      public ItemStack read(FriendlyByteBuf buf) {
         if (buf instanceof net.minecraft.network.RegistryFriendlyByteBuf rb) {
            return ItemStack.OPTIONAL_STREAM_CODEC.decode(rb);
         }

         return ItemStack.OPTIONAL_CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, buf.readNbt()).getOrThrow();
      }

      public Tag write(Provider provider, ItemStack value) {
         return ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), value).getOrThrow();
      }

      public ItemStack read(Provider provider, Tag tag) {
         return ItemStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag).getOrThrow();
      }
   };
   public static final IDataSerializer<Identifier> RESOURCE_LOCATION = new IDataSerializer<Identifier>() {
      public void write(FriendlyByteBuf buf, Identifier value) {
         buf.writeIdentifier(value);
      }

      public Identifier read(FriendlyByteBuf buf) {
         return buf.readIdentifier();
      }

      public Tag write(Provider provider, Identifier value) {
         return StringTag.valueOf(value.toString());
      }

      public Identifier read(Provider provider, Tag tag) {
         return Identifier.tryParse(tag.asString().orElse(""));
      }
   };
}
