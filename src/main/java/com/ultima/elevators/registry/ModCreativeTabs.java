package com.ultima.elevators.registry;

import com.ultima.elevators.UltimaElevators;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UltimaElevators.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ultimaelevators"))
                    .icon(() -> new ItemStack(ModItems.ELEVATOR_CONTROLLER.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.LIFT_LINKER.get());
                        output.accept(ModItems.ELEVATOR_CONTROLLER.get());
                        output.accept(ModItems.MRL_DRIVE.get());
                        output.accept(ModItems.TRACTION_MACHINE.get());
                        output.accept(ModItems.OIL_BUFFER.get());
                        output.accept(ModItems.SPRING_BUFFER.get());
                        output.accept(ModItems.COUNTERWEIGHT_RAIL.get());
                        output.accept(ModItems.SHAFT_FRAME.get());
                        output.accept(ModItems.SHAFT_GLASS.get());
                        output.accept(ModItems.SHAFT_RAIL.get());
                        output.accept(ModItems.ELEVATOR_DOOR.get());
                        output.accept(ModItems.ELEVATOR_DOOR_UPPER.get());
                        output.accept(ModItems.CALL_BUTTON.get());
                        output.accept(ModItems.FLOOR_INDICATOR.get());
                        output.accept(ModItems.DESTINATION_KIOSK.get());
                    })
                    .build());
}
