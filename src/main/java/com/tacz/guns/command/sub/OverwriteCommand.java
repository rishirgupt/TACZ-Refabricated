package com.tacz.guns.command.sub;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.tacz.guns.config.PreLoadConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class OverwriteCommand {
   private static final String OVERWRITE_NAME = "overwrite";
   private static final String ENABLE = "enable";

   public static LiteralArgumentBuilder<CommandSourceStack> get() {
      LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("overwrite");
      RequiredArgumentBuilder<CommandSourceStack, Boolean> enable = Commands.argument("enable", BoolArgumentType.bool());
      reload.then(enable.executes(OverwriteCommand::setOverwrite));
      return reload;
   }

   private static int setOverwrite(CommandContext<CommandSourceStack> context) {
      boolean enable = BoolArgumentType.getBool(context, "enable");
      PreLoadConfig.override.set(!enable);
      PreLoadConfig.override.save();
      if (((CommandSourceStack)context.getSource()).getEntity() instanceof ServerPlayer serverPlayer) {
         if ((Boolean)PreLoadConfig.override.get()) {
            serverPlayer.sendSystemMessage(Component.translatable("commands.tacz.reload.overwrite_off"));
         } else {
            serverPlayer.sendSystemMessage(Component.translatable("commands.tacz.reload.overwrite_on"));
         }
      }

      return 1;
   }
}
