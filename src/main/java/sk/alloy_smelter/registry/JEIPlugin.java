package sk.alloy_smelter.registry;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.integration.jei.SmeltingRecipeCategory;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.screen.ForgeControllerScreen;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(AlloySmelter.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SmeltingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<SmeltingRecipe> recipes = recipeManager.getAllRecipesFor(SmeltingRecipe.Type.INSTANCE);
        registration.addRecipes(SmeltingRecipeCategory.SMELTING_RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Blocks.FORGE_CONTROLLER.get()), SmeltingRecipeCategory.SMELTING_RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ForgeControllerScreen.class, 83, 35, 26, 16, SmeltingRecipeCategory.SMELTING_RECIPE_TYPE);
    }
}
