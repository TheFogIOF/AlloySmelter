package sk.alloy_smelter.integration.rei.smelting;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import sk.alloy_smelter.recipe.SmeltingRecipe;

import java.util.List;

public class DisplaySmeltingT3 extends AbstractCustomDisplay {

    public SmeltingRecipe smeltingRecipe;

    public DisplaySmeltingT3(RecipeHolder<SmeltingRecipe> recipe) {
        super(recipe);
        smeltingRecipe = recipe.value();
    }

    public DisplaySmeltingT3(List<EntryIngredient> input, List<EntryIngredient> output, int smeltingTime, int fuelPerTick, int forgeTier) {
        super(input, output, smeltingTime, fuelPerTick, forgeTier);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return REICategorySmeltingT3.SMELTING_RECIPE_TYPE;
    }
}
