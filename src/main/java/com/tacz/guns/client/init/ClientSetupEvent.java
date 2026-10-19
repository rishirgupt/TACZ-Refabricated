package com.tacz.guns.client.init;

import com.tacz.guns.api.client.other.ThirdPersonManager;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.client.gui.overlay.HeatBarOverlay;
import com.tacz.guns.client.gui.overlay.InteractKeyTextOverlay;
import com.tacz.guns.client.gui.overlay.KillAmountOverlay;
import com.tacz.guns.client.input.AimKey;
import com.tacz.guns.client.input.ConfigKey;
import com.tacz.guns.client.input.CrawlKey;
import com.tacz.guns.client.input.FireSelectKey;
import com.tacz.guns.client.input.InspectKey;
import com.tacz.guns.client.input.InteractKey;
import com.tacz.guns.client.input.MeleeKey;
import com.tacz.guns.client.input.RefitKey;
import com.tacz.guns.client.input.ReloadKey;
import com.tacz.guns.client.input.ShootKey;
import com.tacz.guns.client.input.ZoomKey;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.tooltip.ClientAmmoBoxTooltip;
import com.tacz.guns.client.tooltip.ClientAttachmentItemTooltip;
import com.tacz.guns.client.tooltip.ClientBlockItemTooltip;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import com.tacz.guns.compat.ar.ARCompat;
import com.tacz.guns.compat.controllable.ControllableCompat;
import com.tacz.guns.compat.immediatelyfast.ImmediatelyFastCompat;
import com.tacz.guns.compat.playeranimator.PlayerAnimatorCompat;
import com.tacz.guns.compat.shouldersurfing.ShoulderSurfingCompat;
import com.tacz.guns.compat.zoomify.ZoomifyCompat;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import com.tacz.guns.inventory.tooltip.AttachmentItemTooltip;
import com.tacz.guns.inventory.tooltip.BlockItemTooltip;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

@Environment(EnvType.CLIENT)
public class ClientSetupEvent {
   public static void init() {
      registerKeyMappings();
      registerClientTooltips();
      registerGuiOverlays();
      ClientLifecycleEvents.CLIENT_STARTED.register(ClientSetupEvent::onClientSetup);
      onClientResourceReload();
   }

   public static void registerKeyMappings() {
      registerKeyBinding(InspectKey.INSPECT_KEY);
      registerKeyBinding(ReloadKey.RELOAD_KEY);
      registerKeyBinding(ShootKey.SHOOT_KEY);
      registerKeyBinding(InteractKey.INTERACT_KEY);
      registerKeyBinding(FireSelectKey.FIRE_SELECT_KEY);
      registerKeyBinding(AimKey.AIM_KEY);
      registerKeyBinding(CrawlKey.CRAWL_KEY);
      registerKeyBinding(RefitKey.REFIT_KEY);
      registerKeyBinding(ZoomKey.ZOOM_KEY);
      registerKeyBinding(MeleeKey.MELEE_KEY);
      registerKeyBinding(ConfigKey.OPEN_CONFIG_KEY);
   }

   private static void registerKeyBinding(KeyMapping keyMapping) {
      KeyMappingHelper.registerKeyMapping(keyMapping);
   }

   public static void registerClientTooltips() {
      ClientTooltipComponentCallback.EVENT.register(tooltip -> {
         if (tooltip instanceof GunTooltip gunTooltip) {
            return new ClientGunTooltip(gunTooltip);
         } else if (tooltip instanceof AmmoBoxTooltip ammoBoxTooltip) {
            return new ClientAmmoBoxTooltip(ammoBoxTooltip);
         } else if (tooltip instanceof AttachmentItemTooltip attachmentItemTooltip) {
            return new ClientAttachmentItemTooltip(attachmentItemTooltip);
         } else {
            return tooltip instanceof BlockItemTooltip blockItemTooltip ? new ClientBlockItemTooltip(blockItemTooltip) : null;
         }
      });
   }

   public static void registerGuiOverlays() {
      HudElementRegistry.replaceElement(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.CROSSHAIR, original -> (graphics, deltaTracker) -> {
         java.util.concurrent.atomic.AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean(false);
         com.tacz.guns.client.event.RenderCrosshairEvent.onRenderOverlay(graphics, Minecraft.getInstance().getWindow(), deltaTracker, cancelled);
         if (!cancelled.get()) {
            original.extractRenderState(graphics, deltaTracker);
         }
      });
      HudElementRegistry.replaceElement(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.HOTBAR, original -> (graphics, deltaTracker) -> {
         java.util.concurrent.atomic.AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean(false);
         com.tacz.guns.client.event.PreventsHotbarEvent.onRenderHotbarEvent(cancelled);
         if (!cancelled.get()) {
            original.extractRenderState(graphics, deltaTracker);
         }
      });
      HudElementRegistry.attachElementBefore(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.CROSSHAIR, Identifier.fromNamespaceAndPath("tacz", "scope_view"), com.tacz.guns.client.gui.overlay.ScopeOverlay.INSTANCE::render);
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("tacz", "gun_hud"), GunHudOverlay.INSTANCE::render);
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("tacz", "heat_bar"), HeatBarOverlay.INSTANCE::render);
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("tacz", "interact_key_text"), InteractKeyTextOverlay.INSTANCE::render);
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("tacz", "kill_amount"), KillAmountOverlay.INSTANCE::render);
   }

   public static void onClientSetup(Minecraft minecraft) {
      ThirdPersonManager.registerDefault();
      ShoulderSurfingCompat.init();
      ControllableCompat.init();
      ARCompat.init();
      ZoomifyCompat.init();
      ImmediatelyFastCompat.init();
   }

   private static void registerReloadListener(IdentifiableResourceReloadListener listener) {
      ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(listener.getFabricId(), listener);
   }

   public static void onClientResourceReload() {
      PlayerAnimatorCompat.init();
      ClientAssetsManager.INSTANCE.reloadAndRegister(ClientSetupEvent::registerReloadListener);
      if (PlayerAnimatorCompat.isInstalled()) {
         PlayerAnimatorCompat.registerReloadListener(ClientSetupEvent::registerReloadListener);
      }
   }
}
