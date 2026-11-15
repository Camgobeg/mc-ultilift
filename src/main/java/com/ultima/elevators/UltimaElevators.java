package com.ultima.elevators;

import com.ultima.elevators.registry.ModBlockEntities;
import com.ultima.elevators.registry.ModBlocks;
import com.ultima.elevators.registry.ModCreativeTabs;
import com.ultima.elevators.registry.ModEntities;
import com.ultima.elevators.registry.ModItems;
import com.ultima.elevators.network.ModNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(UltimaElevators.MODID)
public class UltimaElevators {
    public static final String MODID = "ultimaelevators";

    public UltimaElevators() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModNetwork.init();
    }
}
