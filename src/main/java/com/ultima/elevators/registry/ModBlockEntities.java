package com.ultima.elevators.registry;

import com.ultima.elevators.UltimaElevators;
import com.ultima.elevators.block.entity.ControllerBlockEntity;
import com.ultima.elevators.block.entity.DoorBlockEntity;
import com.ultima.elevators.block.entity.FloorIndicatorBlockEntity;
import com.ultima.elevators.block.entity.KioskBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, UltimaElevators.MODID);

    public static final RegistryObject<BlockEntityType<ControllerBlockEntity>> CONTROLLER =
            BLOCK_ENTITIES.register("controller", () -> BlockEntityType.Builder.of(
                    ControllerBlockEntity::new, ModBlocks.ELEVATOR_CONTROLLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<DoorBlockEntity>> DOOR =
            BLOCK_ENTITIES.register("elevator_door", () -> BlockEntityType.Builder.of(
                    DoorBlockEntity::new, ModBlocks.ELEVATOR_DOOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<FloorIndicatorBlockEntity>> FLOOR_INDICATOR =
            BLOCK_ENTITIES.register("floor_indicator", () -> BlockEntityType.Builder.of(
                    FloorIndicatorBlockEntity::new, ModBlocks.FLOOR_INDICATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<KioskBlockEntity>> DESTINATION_KIOSK =
            BLOCK_ENTITIES.register("destination_kiosk", () -> BlockEntityType.Builder.of(
                    KioskBlockEntity::new, ModBlocks.DESTINATION_KIOSK.get()).build(null));
}
