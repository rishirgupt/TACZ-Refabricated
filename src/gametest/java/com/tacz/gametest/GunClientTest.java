package com.tacz.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.world.level.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Dev-only scripted client session. -Dtz.script / build/tz-script.txt: ';' separated steps (cmd:, wait:, shot:, hold:, release:, press:, mouse:, cam:, look:, slot:). */
public class GunClientTest implements FabricClientGameTest {
    private static final Logger LOG = LoggerFactory.getLogger("tz-test");

    @Override
    public void runTest(ClientGameTestContext context) {
        String script = "wait:20;shot:idle";
        java.nio.file.Path scriptFile = java.nio.file.Path.of("..", "tz-script.txt");
        if (java.nio.file.Files.exists(scriptFile)) {
            try {
                script = java.nio.file.Files.readString(scriptFile).trim();
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            TestServerContext server = world.getServer();
            world.getConnection().waitForChunksRender();
            server.runOnServer(s -> s.getPlayerList().getPlayers().get(0).setGameMode(GameType.CREATIVE));
            server.runCommand("tp @p 0.5 -60 0.5 0 10");
            server.runCommand("time set noon");
            context.waitTicks(20);
            for (String step : script.split(";")) {
                run(context, server, step.trim());
            }
            LOG.info("SCRIPT DONE");
            org.apache.logging.log4j.LogManager.shutdown();
            Runtime.getRuntime().halt(0);
        }
    }

    private void run(ClientGameTestContext context, TestServerContext server, String step) {
        if (step.isEmpty()) {
            return;
        }
        String[] p = step.split(":", 2);
        switch (p[0]) {
            case "cmd" -> server.runCommand(p[1]);
            case "wait" -> context.waitTicks(Integer.parseInt(p[1]));
            case "shot" -> context.takeScreenshot(p[1]);
            case "mouse" -> context.getInput().holdMouseFor(0, Integer.parseInt(p[1]));
            case "rmouse" -> context.getInput().holdMouseFor(1, Integer.parseInt(p[1]));
            case "hold" -> context.getInput().holdKey(InputConstants.getKey(p[1]));
            case "release" -> context.getInput().releaseKey(InputConstants.getKey(p[1]));
            case "press" -> context.getInput().holdKeyFor(InputConstants.getKey(p[1]), 3);
            case "cam" -> context.runOnClient(mc -> mc.options.setCameraType(CameraType.values()[Integer.parseInt(p[1])]));
            case "look" -> {
                String[] a = p[1].split(":");
                context.getInput().lookAt(Float.parseFloat(a[0]), Float.parseFloat(a[1]));
            }
            case "hp" -> server.runOnServer(s -> {
                for (net.minecraft.server.level.ServerLevel level : s.getAllLevels()) {
                    level.getAllEntities().forEach(e -> {
                        if (e instanceof net.minecraft.world.entity.LivingEntity le && !(e instanceof net.minecraft.world.entity.player.Player)) {
                            LOG.info("HP {} {} alive={}", e.getType().toShortString(), le.getHealth(), le.isAlive());
                        }
                    });
                }
            });
            case "ammo" -> server.runOnServer(s -> {
                var pl = s.getPlayerList().getPlayers().get(0);
                LOG.info("MAINHAND {} {}", pl.getMainHandItem(), pl.getMainHandItem().getComponents().get(net.minecraft.core.component.DataComponents.CUSTOM_DATA));
            });
            case "craft" -> context.runOnClient(mc -> {
                if (mc.player != null) {
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new com.tacz.guns.network.message.ClientMessageCraft(net.minecraft.resources.Identifier.parse(p[1]), mc.player.containerMenu.containerId));
                }
            });
            case "inv" -> server.runOnServer(s -> {
                var pl2 = s.getPlayerList().getPlayers().get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pl2.getInventory().getContainerSize(); i++) {
                    var it = pl2.getInventory().getItem(i);
                    if (!it.isEmpty()) sb.append(it.getCount()).append("x ").append(it.getItem()).append(" ").append(it.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA)).append("; ");
                }
                LOG.info("INV {}", sb);
            });
            case "probe" -> context.runOnClient(mc -> {
                var id = net.minecraft.resources.Identifier.parse("tacz:ak47");
                LOG.info("PROBE index={} commonIndex={} guns={}", com.tacz.guns.api.TimelessAPI.getClientGunIndex(id).isPresent(), com.tacz.guns.api.TimelessAPI.getCommonGunIndex(id).isPresent(), com.tacz.guns.api.TimelessAPI.getAllClientGunIndex().size());
                var stack = new net.minecraft.world.item.ItemStack(com.tacz.guns.init.ModItems.MODERN_KINETIC_GUN);
                LOG.info("PROBE stackGun {}", mc.player == null ? null : mc.player.getMainHandItem());
            });
            default -> LOG.warn("unknown step {}", step);
        }
    }
}
