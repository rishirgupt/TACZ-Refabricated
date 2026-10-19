package cn.sh1rocu.tacz.compat.rei.display;

import cn.sh1rocu.tacz.compat.rei.REIClientPlugin;
import cn.sh1rocu.tacz.compat.rei.entry.AttachmentQueryEntry;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

public class AttachmentQueryDisplay implements Display {
   private final AttachmentQueryEntry entry;

   public AttachmentQueryDisplay(AttachmentQueryEntry entry) {
      this.entry = entry;
   }

   public AttachmentQueryEntry getEntry() {
      return this.entry;
   }

   public List<EntryIngredient> getInputEntries() {
      List<EntryIngredient> inputs = new ArrayList<>();
      this.entry.getAllowGunStacks().forEach(gun -> inputs.add(EntryIngredients.of(gun)));
      if (!this.entry.getExtraAllowGunStacks().isEmpty()) {
         inputs.add(EntryIngredients.ofItemStacks(this.entry.getExtraAllowGunStacks()));
      }

      return inputs;
   }

   public List<EntryIngredient> getOutputEntries() {
      return List.of(EntryIngredients.of(this.entry.getAttachmentStack()));
   }

   public CategoryIdentifier<?> getCategoryIdentifier() {
      return REIClientPlugin.ATTACHMENT_QUERY;
   }
}
