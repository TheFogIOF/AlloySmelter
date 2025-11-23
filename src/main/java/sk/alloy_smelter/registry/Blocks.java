package sk.alloy_smelter.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.block.ForgeControllerBlock;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class Blocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, AlloySmelter.MOD_ID);

    public static final Supplier<Block> FORGE_CONTROLLER_TIER1 = registerBlock("forge_controller_tier1",
            () -> new ForgeControllerBlock(Block.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.BRICKS).strength(4.0F).lightLevel(litBlockEmission(12)), 1));
    public static final Supplier<Block> FORGE_CONTROLLER_TIER2 = registerBlock("forge_controller_tier2",
            () -> new ForgeControllerBlock(Block.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.BRICKS).strength(5.0F).lightLevel(litBlockEmission(12)), 2));
    public static final Supplier<Block> FORGE_CONTROLLER_TIER3 = registerBlock("forge_controller_tier3",
            () -> new ForgeControllerBlock(Block.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.BRICKS).strength(6.0F).lightLevel(litBlockEmission(12)), 3));

    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return blockState -> blockState.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    private static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        Supplier<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> Supplier<Item> registerBlockItem(String name, Supplier<T> block) {
        return Items.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}