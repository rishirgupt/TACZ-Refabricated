package com.tacz.guns.command.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.resource.CommonAssetsManager;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.time.StopWatch;

public class ReloadCommand {
   private static final String RELOAD_NAME = "reload";

   public static LiteralArgumentBuilder<CommandSourceStack> get() {
      LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload");
      reload.executes(ReloadCommand::reloadAllPack);
      return reload;
   }

   private static int reloadAllPack(CommandContext<CommandSourceStack> context) {
      StopWatch watch = StopWatch.createStarted();
      if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
         reloadClient();
      }

      CommonAssetsManager.reloadAllPack();
      watch.stop();
      double time = watch.getTime(TimeUnit.MICROSECONDS) / 1000.0;
      ((CommandSourceStack)context.getSource()).sendSystemMessage(Component.translatable("commands.tacz.reload.success", new Object[]{time}));
      return 1;
   }

   public static void reloadClient() {
      ClientAssetsManager.reloadAllPack();
   }
}
