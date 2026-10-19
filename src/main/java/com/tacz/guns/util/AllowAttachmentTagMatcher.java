package com.tacz.guns.util;

import com.tacz.guns.resource.CommonAssetsManager;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Pair;

public final class AllowAttachmentTagMatcher {
   private static final String TAG_PREFIX = "#";
   private static final AllowAttachmentTagMatcher.Cache CACHE = new AllowAttachmentTagMatcher.Cache();

   public static boolean match(Identifier gunId, Identifier attachmentId) {
      Pair<Identifier, Identifier> key = Pair.of(gunId, attachmentId);
      return CACHE.allowAttachmentCache().computeIfAbsent(key, AllowAttachmentTagMatcher::match0);
   }

   public static boolean match0(Pair<Identifier, Identifier> record) {
      Identifier gunId = (Identifier)record.getLeft();
      Identifier attachmentId = (Identifier)record.getRight();
      Set<String> allowAttachmentTags = CommonAssetsManager.get().getAllowAttachmentTags(gunId);
      if (allowAttachmentTags != null && !allowAttachmentTags.isEmpty()) {
         AtomicBoolean searchSignal = new AtomicBoolean(false);
         treeSearch(allowAttachmentTags, attachmentId, searchSignal);
         return searchSignal.get();
      } else {
         return false;
      }
   }

   public static boolean matchTag(Identifier tag, Identifier attachmentId) {
      Pair<Identifier, Identifier> key = Pair.of(tag, attachmentId);
      return CACHE.tagMatchCache().computeIfAbsent(key, AllowAttachmentTagMatcher::matchTag0);
   }

   public static boolean matchTag0(Pair<Identifier, Identifier> record) {
      Identifier tag = (Identifier)record.getLeft();
      Identifier attachmentId = (Identifier)record.getRight();
      Set<String> tagContent = CommonAssetsManager.get().getAttachmentTags(tag);
      if (tagContent != null && !tagContent.isEmpty()) {
         AtomicBoolean searchSignal = new AtomicBoolean(false);
         treeSearch(tagContent, attachmentId, searchSignal);
         return searchSignal.get();
      } else {
         return false;
      }
   }

   private static void treeSearch(Set<String> tags, Identifier attachmentId, AtomicBoolean searchSignal) {
      for (String tag : tags) {
         if (tag.startsWith("#")) {
            Identifier tagId = Identifier.parse(tag.substring("#".length()));
            Set<String> attachmentTags = CommonAssetsManager.get().getAttachmentTags(tagId);
            if (attachmentTags != null && !attachmentTags.isEmpty()) {
               treeSearch(attachmentTags, attachmentId, searchSignal);
            }
         } else {
            Identifier matchAttachmentId = Identifier.parse(tag);
            if (attachmentId.equals(matchAttachmentId)) {
               searchSignal.set(true);
               return;
            }
         }
      }
   }

   public static void resetCache() {
      CACHE.allowAttachmentCache().clear();
      CACHE.tagMatchCache().clear();
   }

   public record Cache(Map<Pair<Identifier, Identifier>, Boolean> allowAttachmentCache, Map<Pair<Identifier, Identifier>, Boolean> tagMatchCache) {
      public Cache() {
         this(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
      }
   }
}
