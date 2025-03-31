package sk.alloy_smelter.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.block.ForgeControllerBlockEntity;

import java.util.function.Supplier;

public class BlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AlloySmelter.MOD_ID);

    public static final Supplier<BlockEntityType<ForgeControllerBlockEntity>> FORGE_CONTROLLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("forge_controller_block_entity",
                () -> new BlockEntityType<>(
                        ForgeControllerBlockEntity::new,
                        Blocks.FORGE_CONTROLLER_TIER1.get(),
                        Blocks.FORGE_CONTROLLER_TIER2.get(),
                        Blocks.FORGE_CONTROLLER_TIER3.get()
                )
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}