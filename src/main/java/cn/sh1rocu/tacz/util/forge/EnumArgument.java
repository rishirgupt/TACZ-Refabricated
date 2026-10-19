package cn.sh1rocu.tacz.util.forge;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo.Template;

public class EnumArgument<T extends Enum<T>> implements ArgumentType<T> {
   private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
      (found, constants) -> Component.translatable("commands.tacz.arguments.enum.invalid", new Object[]{constants, found})
   );
   private final Class<T> enumClass;

   public static <R extends Enum<R>> EnumArgument<R> enumArgument(Class<R> enumClass) {
      return new EnumArgument<>(enumClass);
   }

   private EnumArgument(Class<T> enumClass) {
      this.enumClass = enumClass;
   }

   public T parse(StringReader reader) throws CommandSyntaxException {
      String name = reader.readUnquotedString();

      try {
         return Enum.valueOf(this.enumClass, name);
      } catch (IllegalArgumentException e) {
         throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(Arrays.<Enum>stream(this.enumClass.getEnumConstants()).map(Enum::name).toArray()));
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return SharedSuggestionProvider.suggest(Stream.<Enum>of(this.enumClass.getEnumConstants()).map(Enum::name), builder);
   }

   public Collection<String> getExamples() {
      return Stream.<Enum>of(this.enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
   }

   public static class Info<T extends Enum<T>> implements ArgumentTypeInfo<EnumArgument<T>, EnumArgument.Info<T>.Template> {
      public void serializeToNetwork(EnumArgument.Info<T>.Template template, FriendlyByteBuf buffer) {
         buffer.writeUtf(template.enumClass.getName());
      }

      public EnumArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf buffer) {
         try {
            String name = buffer.readUtf();
            return new EnumArgument.Info.Template((Class<T>)Class.forName(name));
         } catch (ClassNotFoundException e) {
            return null;
         }
      }

      public void serializeToJson(EnumArgument.Info<T>.Template template, JsonObject json) {
         json.addProperty("enum", template.enumClass.getName());
      }

      public EnumArgument.Info<T>.Template unpack(EnumArgument<T> argument) {
         return new EnumArgument.Info.Template(argument.enumClass);
      }

      public class Template implements ArgumentTypeInfo.Template<EnumArgument<T>> {
         final Class<T> enumClass;

         Template(Class<T> enumClass) {
            this.enumClass = enumClass;
         }

         public EnumArgument<T> instantiate(CommandBuildContext pStructure) {
            return new EnumArgument<>(this.enumClass);
         }

         public ArgumentTypeInfo<EnumArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }
}
