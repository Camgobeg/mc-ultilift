package com.ultima.elevators.registry;

import com.ultima.elevators.UltimaElevators;
import com.ultima.elevators.item.LiftLinkerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, UltimaElevators.MODID);

    public static final RegistryObject<Item> LIFT_LINKER = ITEMS.register("lift_linker",
            () -> new LiftLinkerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ELEVATOR_CONTROLLER = blockItem(ModBlocks.ELEVATOR_CONTROLLER);
    public static final RegistryObject<Item> SHAFT_FRAME = blockItem(ModBlocks.SHAFT_FRAME);
    public static final RegistryObject<Item> SHAFT_GLASS = blockItem(ModBlocks.SHAFT_GLASS);
    public static final RegistryObject<Item> SHAFT_RAIL = blockItem(ModBlocks.SHAFT_RAIL);
    public static final RegistryObject<Item> COUNTERWEIGHT_RAIL = blockItem(ModBlocks.COUNTERWEIGHT_RAIL);
    public static final RegistryObject<Item> OIL_BUFFER = blockItem(ModBlocks.OIL_BUFFER);
    public static final RegistryObject<Item> SPRING_BUFFER = blockItem(ModBlocks.SPRING_BUFFER);
    public static final RegistryObject<Item> MRL_DRIVE = blockItem(ModBlocks.MRL_DRIVE);
    public static final RegistryObject<Item> TRACTION_MACHINE = blockItem(ModBlocks.TRACTION_MACHINE);
    public static final RegistryObject<Item> ELEVATOR_DOOR = blockItem(ModBlocks.ELEVATOR_DOOR);
    public static final RegistryObject<Item> ELEVATOR_DOOR_UPPER = blockItem(ModBlocks.ELEVATOR_DOOR_UPPER);
    public static final RegistryObject<Item> CALL_BUTTON = blockItem(ModBlocks.CALL_BUTTON);
    public static final RegistryObject<Item> FLOOR_INDICATOR = blockItem(ModBlocks.FLOOR_INDICATOR);
    public static final RegistryObject<Item> DESTINATION_KIOSK = blockItem(ModBlocks.DESTINATION_KIOSK);

    private static RegistryObject<Item> blockItem(RegistryObject<? extends net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
