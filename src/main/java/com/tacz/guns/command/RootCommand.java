package com.tacz.guns.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tacz.guns.command.sub.AttachmentLockCommand;
import com.tacz.guns.command.sub.ConfigCommand;
import com.tacz.guns.command.sub.ConvertCommand;
import com.tacz.guns.command.sub.DebugCommand;
import com.tacz.guns.command.sub.DummyAmmoCommand;
import com.tacz.guns.command.sub.HideTooltipPartCommand;
import com.tacz.guns.command.sub.OverwriteCommand;
import com.tacz.guns.command.sub.ReloadCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class RootCommand {
   private static final String ROOT_NAME = "tacz";

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> root = (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("tacz").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
      root.then(AttachmentLockCommand.get());
      root.then(DebugCommand.get());
      root.then(DummyAmmoCommand.get());
      root.then(OverwriteCommand.get());
      root.then(ReloadCommand.get());
      root.then(HideTooltipPartCommand.get());
      root.then(ConvertCommand.get());
      root.then(ConfigCommand.get());
      dispatcher.register(root);
   }
}
