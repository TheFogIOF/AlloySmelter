package sk.alloy_smelter.integration.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import sk.alloy_smelter.registry.Blocks;

@REIPluginClient
public class REIPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new SmeltingCategoryREI());
        registry.addWorkstations(SmeltingCategoryREI.SMELTING_RECIPE_TYPE, EntryStacks.of(Blocks.FORGE_CONTROLLER.get()));
    }
}