package com.ultima.elevators.registry;

import com.ultima.elevators.UltimaElevators;
import com.ultima.elevators.block.CallButtonBlock;
import com.ultima.elevators.block.DestinationKioskBlock;
import com.ultima.elevators.block.ElevatorControllerBlock;
import com.ultima.elevators.block.ElevatorDoorBlock;
import com.ultima.elevators.block.ElevatorDoorUpperBlock;
import com.ultima.elevators.block.FloorIndicatorBlock;
import com.ultima.elevators.block.MrlDriveBlock;
import com.ultima.elevators.block.OilBufferBlock;
import com.ultima.elevators.block.ShaftFrameBlock;
import com.ultima.elevators.block.ShaftGlassBlock;
import com.ultima.elevators.block.ShaftRailBlock;
import com.ultima.elevators.block.SpringBufferBlock;
import com.ultima.elevators.block.CounterweightRailBlock;
import com.ultima.elevators.block.TractionMachineBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, UltimaElevators.MODID);

    public static final RegistryObject<Block> ELEVATOR_CONTROLLER = BLOCKS.register("elevator_controller",
            () -> new ElevatorControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.5F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> SHAFT_FRAME = BLOCKS.register("shaft_frame",
            () -> new ShaftFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.METAL).noCollission()));

    public static final RegistryObject<Block> SHAFT_GLASS = BLOCKS.register("shaft_glass",
            () -> new ShaftGlassBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.8F, 4.0F).sound(SoundType.GLASS)
                    .noOcclusion().noCollission()));

    public static final RegistryObject<Block> SHAFT_RAIL = BLOCKS.register("shaft_rail",
            () -> new ShaftRailBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.METAL).noCollission()));

    public static final RegistryObject<Block> COUNTERWEIGHT_RAIL = BLOCKS.register("counterweight_rail",
            () -> new CounterweightRailBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY).strength(2.5F, 6.0F).sound(SoundType.METAL)));

    public static final RegistryObject<Block> OIL_BUFFER = BLOCKS.register("oil_buffer",
            () -> new OilBufferBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK).strength(3.0F, 8.0F).sound(SoundType.METAL)));

    public static final RegistryObject<Block> SPRING_BUFFER = BLOCKS.register("spring_buffer",
            () -> new SpringBufferBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY).strength(3.0F, 8.0F).sound(SoundType.METAL)));

    public static final RegistryObject<Block> MRL_DRIVE = BLOCKS.register("mrl_drive",
            () -> new MrlDriveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> TRACTION_MACHINE = BLOCKS.register("traction_machine",
            () -> new TractionMachineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELEVATOR_DOOR = BLOCKS.register("elevator_door",
            () -> new ElevatorDoorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(2.5F, 8.0F).sound(SoundType.METAL).noOcclusion()));

    public static final RegistryObject<Block> ELEVATOR_DOOR_UPPER = BLOCKS.register("elevator_door_upper",
            () -> new ElevatorDoorUpperBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(2.5F, 8.0F).sound(SoundType.METAL).noOcclusion()));

    public static final RegistryObject<Block> CALL_BUTTON = BLOCKS.register("call_button",
            () -> new CallButtonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(1.0F, 4.0F).sound(SoundType.METAL).noOcclusion()));

    public static final RegistryObject<Block> FLOOR_INDICATOR = BLOCKS.register("floor_indicator",
            () -> new FloorIndicatorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(1.0F, 4.0F).sound(SoundType.METAL).noOcclusion()));

    public static final RegistryObject<Block> DESTINATION_KIOSK = BLOCKS.register("destination_kiosk",
            () -> new DestinationKioskBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(1.5F, 4.0F).sound(SoundType.METAL).noOcclusion()));
}
