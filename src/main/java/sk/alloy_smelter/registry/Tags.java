package sk.alloy_smelter.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import sk.alloy_smelter.AlloySmelter;

public class Tags {
    public static final TagKey<Item> ALLOY_SMELTER_FUEL = ItemTag("alloy_smelter_fuel");
    public static final TagKey<Block> ALLOY_SMELTER_BLOCKS_TIER1 = BlockTag("alloy_smelter_blocks_tier1");
    public static final TagKey<Block> ALLOY_SMELTER_BLOCKS_TIER2 = BlockTag("alloy_smelter_blocks_tier2");
    public static final TagKey<Block> ALLOY_SMELTER_BLOCKS_TIER3 = BlockTag("alloy_smelter_blocks_tier3");
    public static final TagKey<Block> FORGE_CONTROLLER = BlockTag("forge_controller");

    private static TagKey<Item> ItemTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, name));
    }

    private static TagKey<Block> BlockTag(String name) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, name));
    }
}
