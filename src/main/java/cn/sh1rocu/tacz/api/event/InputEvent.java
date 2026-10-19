package cn.sh1rocu.tacz.api.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.ApiStatus.Internal;

@Environment(EnvType.CLIENT)
public abstract class InputEvent extends BaseEvent {
   public interface IKMTCallback {
      void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered var1);
   }

   public static class InteractionKeyMappingTriggered extends InputEvent implements ICancellableEvent {
      public static final Event<InputEvent.IKMTCallback> EVENT = EventFactory.createArrayBacked(InputEvent.IKMTCallback.class, callbacks -> event -> {
         for (InputEvent.IKMTCallback e : callbacks) {
            e.onInteractionKeyMappingTriggered(event);
         }
      });
      private final int button;
      private final KeyMapping keyMapping;
      private final InteractionHand hand;
      private boolean handSwing = true;

      public InteractionKeyMappingTriggered(int button, KeyMapping keyMapping, InteractionHand hand) {
         this.button = button;
         this.keyMapping = keyMapping;
         this.hand = hand;
      }

      public void setSwingHand(boolean value) {
         this.handSwing = value;
      }

      public boolean shouldSwingHand() {
         return this.handSwing;
      }

      public InteractionHand getHand() {
         return this.hand;
      }

      public boolean isAttack() {
         return this.button == 0;
      }

      public boolean isUseItem() {
         return this.button == 1;
      }

      public boolean isPickBlock() {
         return this.button == 2;
      }

      public KeyMapping getKeyMapping() {
         return this.keyMapping;
      }
   }

   public static class Key extends InputEvent {
      public static final Event<InputEvent.KeyCallback> EVENT = EventFactory.createArrayBacked(InputEvent.KeyCallback.class, callbacks -> event -> {
         for (InputEvent.KeyCallback e : callbacks) {
            e.onKey(event);
         }
      });
      private final int key;
      private final int scanCode;
      private final int action;
      private final int modifiers;

      @Internal
      public Key(int key, int scanCode, int action, int modifiers) {
         this.key = key;
         this.scanCode = scanCode;
         this.action = action;
         this.modifiers = modifiers;
      }

      public int getKey() {
         return this.key;
      }

      public int getScanCode() {
         return this.scanCode;
      }

      public int getAction() {
         return this.action;
      }

      public int getModifiers() {
         return this.modifiers;
      }
   }

   public interface KeyCallback {
      void onKey(InputEvent.Key var1);
   }

   public static class MouseButton extends InputEvent {
      public static final Event<InputEvent.MouseCallback> EVENT = EventFactory.createArrayBacked(InputEvent.MouseCallback.class, callbacks -> event -> {
         for (InputEvent.MouseCallback e : callbacks) {
            e.onMouse(event);
         }
      });
      private final int button;
      private final int action;
      private final int modifiers;

      @Internal
      protected MouseButton(int button, int action, int modifiers) {
         this.button = button;
         this.action = action;
         this.modifiers = modifiers;
      }

      public int getButton() {
         return this.button;
      }

      public int getAction() {
         return this.action;
      }

      public int getModifiers() {
         return this.modifiers;
      }

      public static class Post extends InputEvent.MouseButton {
         public static final Event<InputEvent.MousePostCallback> EVENT = EventFactory.createArrayBacked(
            InputEvent.MousePostCallback.class, callbacks -> event -> {
               for (InputEvent.MousePostCallback e : callbacks) {
                  e.onMousePost(event);
               }
            }
         );

         @Internal
         public Post(int button, int action, int modifiers) {
            super(button, action, modifiers);
         }
      }

      public static class Pre extends InputEvent.MouseButton implements ICancellableEvent {
         public static final Event<InputEvent.MousePreCallback> EVENT = EventFactory.createArrayBacked(
            InputEvent.MousePreCallback.class, callbacks -> event -> {
               for (InputEvent.MousePreCallback e : callbacks) {
                  e.onMousePre(event);
               }
            }
         );

         @Internal
         public Pre(int button, int action, int modifiers) {
            super(button, action, modifiers);
         }
      }
   }

   public interface MouseCallback {
      void onMouse(InputEvent.MouseButton var1);
   }

   public interface MousePostCallback {
      void onMousePost(InputEvent.MouseButton.Post var1);
   }

   public interface MousePreCallback {
      void onMousePre(InputEvent.MouseButton.Pre var1);
   }
}
