package com.ultima.elevators.client;

import com.ultima.elevators.UltimaElevators;
import com.ultima.elevators.client.renderer.DoorRenderer;
import com.ultima.elevators.client.renderer.ElevatorCarRenderer;
import com.ultima.elevators.client.renderer.FloorIndicatorRenderer;
import com.ultima.elevators.client.renderer.KioskRenderer;
import com.ultima.elevators.registry.ModBlockEntities;
import com.ultima.elevators.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltimaElevators.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ELEVATOR_CAR.get(), ElevatorCarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DOOR.get(), DoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLOOR_INDICATOR.get(), FloorIndicatorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DESTINATION_KIOSK.get(), KioskRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ElevatorCarRenderer.LAYER, com.ultima.elevators.client.model.ElevatorModels::createLayer);
        event.registerLayerDefinition(DoorRenderer.LAYER, com.ultima.elevators.client.model.DoorModels::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(ClientTickHandler.OPEN_COP);
    }
}
