package cn.sh1rocu.tacz.compat.rei.entry;

import com.google.common.collect.Lists;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;

public class AttachmentQueryEntry {
   private final ItemStack attachmentStack;
   private List<ItemStack> allowGunStacks;
   private List<ItemStack> extraAllowGunStacks;
   private Identifier attachmentId;

   public AttachmentQueryEntry(Identifier attachmentId, GunTabType type) {
      this.attachmentStack = AttachmentItemBuilder.create().setId(attachmentId).build();
      this.allowGunStacks = Lists.newArrayList();
      this.extraAllowGunStacks = Lists.newArrayList();
      this.addAllAllowGuns(type);
      this.dividedGuns();
   }

   public static List<AttachmentQueryEntry> getAllAttachmentQueryEntries() {
      List<AttachmentQueryEntry> entries = Lists.newArrayList();
      TimelessAPI.getAllCommonAttachmentIndex().forEach(entry -> {
         if (!((CommonAttachmentIndex)entry.getValue()).getPojo().isHidden()) {
            for (GunTabType tabType : GunTabType.values()) {
               AttachmentQueryEntry queryEntry = new AttachmentQueryEntry(entry.getKey(), tabType);
               if (!queryEntry.getAllowGunStacks().isEmpty()) {
                  entries.add(queryEntry);
               }
            }
         }
      });
      return entries;
   }

   public Identifier getAttachmentId() {
      return this.attachmentId;
   }

   public ItemStack getAttachmentStack() {
      return this.attachmentStack;
   }

   public List<ItemStack> getAllowGunStacks() {
      return this.allowGunStacks;
   }

   public List<ItemStack> getExtraAllowGunStacks() {
      return this.extraAllowGunStacks;
   }

   private void addAllAllowGuns(GunTabType type) {
      TimelessAPI.getAllCommonGunIndex().forEach(entry -> {
         String tabType = type.name().toLowerCase(Locale.US);
         String gunType = entry.getValue().getType();
         if (tabType.equals(gunType)) {
            ItemStack gun = GunItemBuilder.create().setId(entry.getKey()).build(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
            if (!(gun.getItem() instanceof IGun iGun)) {
               return;
            }

            if (iGun.allowAttachment(gun, this.attachmentStack)) {
               this.allowGunStacks.add(gun);
            }
         }
      });
   }

   private void dividedGuns() {
      int size = this.allowGunStacks.size();
      if (size >= 60) {
         this.extraAllowGunStacks = this.allowGunStacks.subList(60, size);
         this.allowGunStacks = this.allowGunStacks.subList(0, 60);
      }
   }
}
