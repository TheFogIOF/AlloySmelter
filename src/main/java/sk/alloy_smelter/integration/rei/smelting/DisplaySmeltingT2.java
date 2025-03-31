package sk.alloy_smelter.integration.rei.smelting;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import sk.alloy_smelter.recipe.SmeltingRecipe;

import java.util.List;
import java.util.Optional;

public class DisplaySmeltingT2 extends AbstractCustomDisplay {
    public static DisplaySerializer<DisplaySmeltingT2> SERIALIZER = serializer(DisplaySmeltingT2::new);

    public DisplaySmeltingT2(RecipeHolder<SmeltingRecipe> recipe) {
        super(recipe);
    }

    public DisplaySmeltingT2(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> id, int smeltingTime, int fuelPerTick, int forgeTier) {
        super(input, output, id, smeltingTime, fuelPerTick, forgeTier);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return REICategorySmeltingT2.SMELTING_RECIPE_TYPE;
    }

    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
