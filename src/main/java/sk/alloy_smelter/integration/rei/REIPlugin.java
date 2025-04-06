package sk.alloy_smelter.integration.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import sk.alloy_smelter.integration.rei.smelting.*;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.registry.Blocks;
import sk.alloy_smelter.registry.RecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@REIPluginClient
public class REIPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new REICategorySmeltingT1());
        registry.add(new REICategorySmeltingT2());
        registry.add(new REICategorySmeltingT3());
        registry.addWorkstations(REICategorySmeltingT1.SMELTING_RECIPE_TYPE, EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER1.get()), EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER2.get()), EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER3.get()));
        registry.addWorkstations(REICategorySmeltingT2.SMELTING_RECIPE_TYPE, EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER2.get()), EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER3.get()));
        registry.addWorkstations(REICategorySmeltingT3.SMELTING_RECIPE_TYPE, EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER3.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        RecipeManager recipeManager = registry.getRecipeManager();
        Stream<RecipeHolder<?>> allRecipes = recipeManager.getRecipes().stream();

        List<RecipeHolder<SmeltingRecipe>> smeltingRecipes = new ArrayList<>();

        allRecipes.filter(recipe -> recipe.value().getType() == RecipeTypes.SMELTING.get()).forEach(r -> smeltingRecipes.add((RecipeHolder<SmeltingRecipe>) r));
        smeltingRecipes.forEach(recipe -> {
            if (recipe.value().getRequiredTier() == 1) registry.add(new DisplaySmeltingT1(recipe), recipe);
            if (recipe.value().getRequiredTier() == 2) registry.add(new DisplaySmeltingT2(recipe), recipe);
            if (recipe.value().getRequiredTier() == 3) registry.add(new DisplaySmeltingT3(recipe), recipe);
        });
    }
}
