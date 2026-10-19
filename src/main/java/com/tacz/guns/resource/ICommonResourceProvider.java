package com.tacz.guns.resource;

import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.index.CommonBlockIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource.pojo.data.block.BlockData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;

public interface ICommonResourceProvider {
   @Nullable
   GunData getGunData(Identifier var1);

   @Nullable
   AttachmentData getAttachmentData(Identifier var1);

   @Nullable
   BlockData getBlockData(Identifier var1);

   @Nullable
   RecipeFilter getRecipeFilter(Identifier var1);

   @Nullable
   CommonGunIndex getGunIndex(Identifier var1);

   @Nullable
   CommonAmmoIndex getAmmoIndex(Identifier var1);

   @Nullable
   CommonAttachmentIndex getAttachmentIndex(Identifier var1);

   @Nullable
   CommonBlockIndex getBlockIndex(Identifier var1);

   @Nullable
   LuaTable getScript(Identifier var1);

   Set<Entry<Identifier, CommonGunIndex>> getAllGuns();

   Set<Entry<Identifier, CommonAmmoIndex>> getAllAmmos();

   Set<Entry<Identifier, CommonAttachmentIndex>> getAllAttachments();

   Set<Entry<Identifier, CommonBlockIndex>> getAllBlocks();

   Set<String> getAttachmentTags(Identifier var1);

   Set<String> getAllowAttachmentTags(Identifier var1);
}
