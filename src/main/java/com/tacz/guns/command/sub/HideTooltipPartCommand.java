package com.tacz.guns.command.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.item.GunTooltipPart;
import java.util.Collection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;

public class HideTooltipPartCommand {
   private static final String HIDE_TOOLTIP_PART_NAME = "hide_tooltip_part";
   private static final String ENTITY = "target";
   private static final String MASK = "mask";

   public static LiteralArgumentBuilder<CommandSourceStack> get() {
      LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal("hide_tooltip_part");
      RequiredArgumentBuilder<CommandSourceStack, EntitySelector> entities = Commands.argument("target", EntityArgument.entities());
      RequiredArgumentBuilder<CommandSourceStack, Integer> part = Commands.argument("mask", IntegerArgumentType.integer(0));
      base.then(entities.then(part.executes(HideTooltipPartCommand::setHide)));
      return base;
   }

   private static int setHide(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      Collection<? extends Entity> entities = EntityArgument.getEntities(context, "target");
      int cnt = 0;
      int mask = IntegerArgumentType.getInteger(context, "mask");

      for (Entity entity : entities) {
         if (entity instanceof LivingEntity living) {
            ItemStack stack = living.getMainHandItem();
            if (stack.getItem() instanceof IGun) {
               GunTooltipPart.setHideFlags(stack, mask);
               cnt++;
            }
         }
      }

      return cnt;
   }
}
