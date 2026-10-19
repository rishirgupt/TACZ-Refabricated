package com.tacz.guns.resource.pojo.data.block;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class BlockData {
   @SerializedName("filter")
   @NotNull
   private Identifier filter = Identifier.fromNamespaceAndPath("tacz", "default");
   @SerializedName("tabs")
   private List<TabConfig> tabs = new ArrayList<>();

   @NotNull
   public Identifier getFilter() {
      return this.filter;
   }

   @NotNull
   public List<TabConfig> getTabs() {
      return this.tabs.isEmpty() ? TabConfig.defaultTabs() : this.tabs;
   }
}
