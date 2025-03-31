package sk.alloy_smelter.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.recipe.SmeltingRecipe;

import java.util.function.Supplier;

public class RecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, AlloySmelter.MOD_ID);

    public static final Supplier<RecipeType<SmeltingRecipe>> SMELTING = RECIPE_TYPES.register("smelting", () -> registerRecipeType("smelting"));

    public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(final String identifier) {
        return new RecipeType<>()
        {
            public String toString() {
                return AlloySmelter.MOD_ID + ":" + identifier;
            }
        };
    }
}
