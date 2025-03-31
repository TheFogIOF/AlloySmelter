package sk.alloy_smelter.integration.rei.smelting;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import sk.alloy_smelter.recipe.SmeltingRecipe;

import java.util.List;

public class DisplaySmeltingT2 extends AbstractCustomDisplay {

    public SmeltingRecipe smeltingRecipe;

    public DisplaySmeltingT2(RecipeHolder<SmeltingRecipe> recipe) {
        super(recipe);
        smeltingRecipe = recipe.value();
    }

    public DisplaySmeltingT2(List<EntryIngredient> input, List<EntryIngredient> output, int smeltingTime, int fuelPerTick, int forgeTier) {
        super(input, output, smeltingTime, fuelPerTick, forgeTier);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return REICategorySmeltingT2.SMELTING_RECIPE_TYPE;
    }
}
