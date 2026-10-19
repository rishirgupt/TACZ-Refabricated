package cn.sh1rocu.tacz.compat.modmenu;

import com.tacz.guns.compat.cloth.MenuIntegration;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuApiImpl implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return FabricLoader.getInstance().isModLoaded("cloth-config") ? parent -> MenuIntegration.getConfigBuilder().setParentScreen(parent).build() : null;
   }
}
