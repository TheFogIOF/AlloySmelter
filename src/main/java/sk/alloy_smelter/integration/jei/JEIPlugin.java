package sk.alloy_smelter.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.integration.rei.smelting.DisplaySmeltingT2;
import sk.alloy_smelter.integration.rei.smelting.DisplaySmeltingT3;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.registry.Blocks;
import sk.alloy_smelter.registry.RecipeTypes;
import sk.alloy_smelter.screen.ForgeControllerScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JEICategorySmeltingT1(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new JEICategorySmeltingT2(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new JEICategorySmeltingT3(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        Stream<RecipeHolder<?>> allRecipes = recipeManager.getRecipes().stream();

        List<SmeltingRecipe> smeltingRecipes = new ArrayList<>();

        allRecipes.filter(recipe -> recipe.value().getType() == RecipeTypes.SMELTING.get()).forEach(r -> smeltingRecipes.add((SmeltingRecipe) r.value()));
        registration.addRecipes(JEICategorySmeltingT1.SMELTING_RECIPE_TYPE, smeltingRecipes.stream().filter(recipe -> recipe.getRequiredTier() == 1).toList());
        registration.addRecipes(JEICategorySmeltingT2.SMELTING_RECIPE_TYPE, smeltingRecipes.stream().filter(recipe -> recipe.getRequiredTier() == 2).toList());
        registration.addRecipes(JEICategorySmeltingT3.SMELTING_RECIPE_TYPE, smeltingRecipes.stream().filter(recipe -> recipe.getRequiredTier() == 3).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Blocks.FORGE_CONTROLLER_TIER1.get()), JEICategorySmeltingT1.SMELTING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Blocks.FORGE_CONTROLLER_TIER2.get()), JEICategorySmeltingT1.SMELTING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Blocks.FORGE_CONTROLLER_TIER3.get()), JEICategorySmeltingT1.SMELTING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Blocks.FORGE_CONTROLLER_TIER2.get()), JEICategorySmeltingT2.SMELTING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Blocks.FORGE_CONTROLLER_TIER3.get()), JEICategorySmeltingT2.SMELTING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Blocks.FORGE_CONTROLLER_TIER3.get()), JEICategorySmeltingT3.SMELTING_RECIPE_TYPE);
    }
}
