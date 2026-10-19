package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.event.AddPackFindersEvent;
import cn.sh1rocu.tacz.api.mixin.PackRepositoryExtension;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes gun packs part of the data pack set of newly created worlds. */
@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
   @Inject(method = "createDefaultLoadConfig", at = @At("HEAD"))
   private static void tacz$addPacks(PackRepository repository, WorldDataConfiguration config, CallbackInfoReturnable<WorldLoader.InitConfig> cir) {
      AddPackFindersEvent event = new AddPackFindersEvent(PackType.SERVER_DATA, ((PackRepositoryExtension)repository)::tacz$addPackFinder, false);
      ((AddPackFindersEvent.Callback)AddPackFindersEvent.CALLBACK.invoker()).onAddPackFinders(event);
   }
}
