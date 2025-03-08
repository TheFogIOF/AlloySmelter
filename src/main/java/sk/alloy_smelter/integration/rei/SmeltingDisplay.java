package sk.alloy_smelter.integration.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.crafting.RecipeHolder;
import sk.alloy_smelter.recipe.SmeltingRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmeltingDisplay extends BasicDisplay {

    public SmeltingRecipe smeltingRecipe;

    public SmeltingDisplay(RecipeHolder<SmeltingRecipe> recipe) {
        super(getInput(recipe.value()), getOutput(recipe.value()));
        smeltingRecipe = recipe.value();
    }

    private static List<EntryIngredient> getInput(SmeltingRecipe recipe) {
        if (recipe == null) return Collections.emptyList();
        List<EntryIngredient> list = new ArrayList<>();
        list.add(EntryIngredients.ofIngredient(recipe.getMaterials().get(0).ingredient()));
        if (recipe.getMaterials().size() > 1) list.add(EntryIngredients.ofIngredient(recipe.getMaterials().get(1).ingredient()));
        return list;
    }

    private static List<EntryIngredient> getOutput(SmeltingRecipe recipe) {
        if (recipe == null) return Collections.emptyList();
        List<EntryIngredient> list = new ArrayList<>();
        list.add(EntryIngredient.of(EntryStacks.of(recipe.getOutput())));
        return list;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return SmeltingCategoryREI.SMELTING_RECIPE_TYPE;
    }
}
