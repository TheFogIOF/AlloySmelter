package sk.alloy_smelter.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.block.ForgeControllerBlock;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class Blocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AlloySmelter.MOD_ID);

    public static final RegistryObject<Block> FORGE_CONTROLLER_TIER1 = registerBlock("forge_controller_tier1",
() -> new ForgeControllerBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.BLACKSTONE).lightLevel(litBlockEmission(12)), 1));
    public static final RegistryObject<Block> FORGE_CONTROLLER_TIER2 = registerBlock("forge_controller_tier2",
            () -> new ForgeControllerBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.BLACKSTONE).lightLevel(litBlockEmission(12)), 2));
    public static final RegistryObject<Block> FORGE_CONTROLLER_TIER3 = registerBlock("forge_controller_tier3",
            () -> new ForgeControllerBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.BLACKSTONE).lightLevel(litBlockEmission(12)), 3));

    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return blockState -> blockState.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return Items.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}