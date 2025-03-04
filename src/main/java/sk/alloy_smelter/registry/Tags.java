package sk.alloy_smelter.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import sk.alloy_smelter.AlloySmelter;

public class Tags {
    public static class Blocks {
        public static final TagKey<Block> ALLOY_SMELTER_BLOCKS = tag("alloy_smelter_blocks");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> ALLOY_SMELTER_FUEL = tag("alloy_smelter_fuel");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, name));
        }
    }
}
