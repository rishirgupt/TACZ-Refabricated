package com.tacz.guns.command.sub;

import cn.sh1rocu.tacz.util.forge.EnumArgument;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.tacz.guns.config.sync.SyncConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ConfigCommand {
   private static final String CONFIG_NAME = "config";
   private static final String KEY = "key";
   private static final String ENABLE = "state";

   public static LiteralArgumentBuilder<CommandSourceStack> get() {
      LiteralArgumentBuilder<CommandSourceStack> config = Commands.literal("config");
      RequiredArgumentBuilder<CommandSourceStack, ConfigCommand.ConfigKey> configKey = Commands.argument(
         "key", EnumArgument.enumArgument(ConfigCommand.ConfigKey.class)
      );
      RequiredArgumentBuilder<CommandSourceStack, Boolean> state = Commands.argument("state", BoolArgumentType.bool());
      return (LiteralArgumentBuilder<CommandSourceStack>)config.then(configKey.then(state.executes(ConfigCommand::setConfig)));
   }

   private static int setConfig(CommandContext<CommandSourceStack> context) {
      ConfigCommand.ConfigKey key = (ConfigCommand.ConfigKey)context.getArgument("key", ConfigCommand.ConfigKey.class);
      boolean state = BoolArgumentType.getBool(context, "state");
      if (key == null) {
         return 0;
      }

      switch (key) {
         case defaultTableLimit:
            SyncConfig.ENABLE_TABLE_FILTER.set(state);
            SyncConfig.ENABLE_TABLE_FILTER.save();
            break;
         case serverShootNetworkCheck:
            SyncConfig.SERVER_SHOOT_NETWORK_V.set(state);
            SyncConfig.SERVER_SHOOT_NETWORK_V.save();
            break;
         case serverShootCooldownCheck:
            SyncConfig.SERVER_SHOOT_COOLDOWN_V.set(state);
            SyncConfig.SERVER_SHOOT_COOLDOWN_V.save();
      }

      ((CommandSourceStack)context.getSource()).sendSystemMessage(Component.translatable(key.lang + "." + (state ? "enabled" : "disabled")));
      return 1;
   }

   public enum ConfigKey {
      defaultTableLimit("commands.tacz.config.default_table_limit"),
      serverShootNetworkCheck("commands.tacz.config.server_shoot_network_check"),
      serverShootCooldownCheck("commands.tacz.config.server_shoot_cooldown_check");

      public final String lang;

      ConfigKey(String lang) {
         this.lang = lang;
      }
   }
}
