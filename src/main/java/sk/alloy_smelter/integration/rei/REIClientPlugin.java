package sk.alloy_smelter.integration.rei;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import sk.alloy_smelter.integration.rei.smelting.REICategorySmeltingT1;
import sk.alloy_smelter.integration.rei.smelting.REICategorySmeltingT2;
import sk.alloy_smelter.integration.rei.smelting.REICategorySmeltingT3;
import sk.alloy_smelter.registry.Blocks;

@REIPluginClient
public class REIClientPlugin implements me.shedaniel.rei.api.client.plugins.REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new REICategorySmeltingT1());
        registry.add(new REICategorySmeltingT2());
        registry.add(new REICategorySmeltingT3());
        registry.addWorkstations(REICategorySmeltingT1.SMELTING_RECIPE_TYPE, EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER1.get()), EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER2.get()), EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER3.get()));
        registry.addWorkstations(REICategorySmeltingT2.SMELTING_RECIPE_TYPE, EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER2.get()), EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER3.get()));
        registry.addWorkstations(REICategorySmeltingT3.SMELTING_RECIPE_TYPE, EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER3.get()));
    }
}