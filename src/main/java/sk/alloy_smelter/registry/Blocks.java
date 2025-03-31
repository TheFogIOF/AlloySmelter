package sk.alloy_smelter.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.block.ForgeControllerBlock;

import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraft.resources.ResourceKey.createRegistryKey;

public class Blocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AlloySmelter.MOD_ID);

    public static final DeferredBlock<ForgeControllerBlock> FORGE_CONTROLLER_TIER1 = registerBlock("forge_controller_tier1",
            registry -> new ForgeControllerBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.BLACKSTONE).setId(ResourceKey.create(Registries.BLOCK, registry)), 1)
    );

    public static final DeferredBlock<ForgeControllerBlock> FORGE_CONTROLLER_TIER2 = registerBlock("forge_controller_tier2",
            registry -> new ForgeControllerBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.BLACKSTONE).setId(ResourceKey.create(Registries.BLOCK, registry)), 2)
    );

    public static final DeferredBlock<ForgeControllerBlock> FORGE_CONTROLLER_TIER3 = registerBlock("forge_controller_tier3",
            registry -> new ForgeControllerBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.BLACKSTONE).setId(ResourceKey.create(Registries.BLOCK, registry)), 3)
    );

    public static <B extends Block> DeferredBlock<B> registerBlock(String name, Function<ResourceLocation, ? extends B> func) {
        DeferredBlock<B> toReturn = BLOCKS.register(name, func);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<T> block) {
        return Items.ITEMS.registerSimpleBlockItem(name, block);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}