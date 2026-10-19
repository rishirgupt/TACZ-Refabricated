package com.tacz.guns.util;

import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;

public class TacPathVisitor extends SimpleFileVisitor<Path> {
   private final File root;
   private final String namespace;
   private final String suffix;
   private final BiConsumer<Identifier, Path> consumer;

   public TacPathVisitor(File root, String namespace, String suffix, BiConsumer<Identifier, Path> consumer) {
      this.root = root;
      this.namespace = namespace;
      this.suffix = suffix;
      this.consumer = consumer;
   }

   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      if (file.toFile().getName().endsWith(this.suffix)) {
         String path = PathHandler.getPath(this.root.toPath(), file, this.suffix);
         Identifier id = Identifier.fromNamespaceAndPath(this.namespace, path);
         this.consumer.accept(id, file);
      }

      return FileVisitResult.CONTINUE;
   }
}
