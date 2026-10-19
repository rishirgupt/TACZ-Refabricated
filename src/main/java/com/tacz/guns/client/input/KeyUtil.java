package com.tacz.guns.client.input;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.resources.Identifier;

/** Small adapter between the Forge-like integer based input events and the 26.2 input event objects. */
public final class KeyUtil {
   public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("tacz", "main"));

   private KeyUtil() {
   }

   public static boolean matches(KeyMapping mapping, int key, int scanCode) {
      return mapping.matches(new KeyEvent(key, scanCode, 0));
   }

   public static boolean matchesMouse(KeyMapping mapping, int button) {
      return mapping.matchesMouse(new MouseButtonEvent(0.0, 0.0, new MouseButtonInfo(button, 0)));
   }
}
