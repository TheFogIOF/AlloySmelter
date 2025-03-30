package sk.alloy_smelter.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.block.ForgeControllerBlockEntity;

public class BlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AlloySmelter.MOD_ID);

    public static final RegistryObject<BlockEntityType<ForgeControllerBlockEntity>> FORGE_CONTROLLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("forge_controller_block_entity", () ->
                    BlockEntityType.Builder.of(ForgeControllerBlockEntity::new,
                            Blocks.FORGE_CONTROLLER.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}