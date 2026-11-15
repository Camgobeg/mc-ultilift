package com.ultima.elevators.client;

import com.ultima.elevators.UltimaElevators;
import com.ultima.elevators.entity.ElevatorCarEntity;
import com.ultima.elevators.network.C2SCopActionPacket;
import com.ultima.elevators.network.ModNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Client tick: R opens the Car Operating Panel while riding. */
@Mod.EventBusSubscriber(modid = UltimaElevators.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientTickHandler {
    private ClientTickHandler() {}

    public static final KeyMapping OPEN_COP = new KeyMapping(
            "key.ultimaelevators.open_cop", GLFW.GLFW_KEY_R, "key.categories.ultimaelevators");

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        while (OPEN_COP.consumeClick()) {
            if (mc.screen == null) {
                AABB box = mc.player.getBoundingBox().inflate(3.0);
                List<ElevatorCarEntity> cabs = mc.level.getEntitiesOfClass(ElevatorCarEntity.class, box,
                        e -> e.distanceToSqr(mc.player) < 4.5);
                if (!cabs.isEmpty()) {
                    ModNetwork.sendToServer(new C2SCopActionPacket(cabs.get(0).getId(), 3, -1));
                }
            }
        }
    }
}
