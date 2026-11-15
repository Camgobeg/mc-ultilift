package com.ultima.elevators.registry;

import com.ultima.elevators.UltimaElevators;
import com.ultima.elevators.entity.ElevatorCarEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UltimaElevators.MODID);

    @SuppressWarnings("removal")
    public static final RegistryObject<EntityType<ElevatorCarEntity>> ELEVATOR_CAR =
            ENTITY_TYPES.register("elevator_car", () -> EntityType.Builder
                    .<ElevatorCarEntity>of(ElevatorCarEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.5F)
                    .fireImmune()
                    .noSummon()
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("ultimaelevators:elevator_car"));
}
