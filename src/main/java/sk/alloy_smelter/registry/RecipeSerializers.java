package sk.alloy_smelter.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.recipe.SmeltingRecipe;

import java.util.function.Supplier;

public class RecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, AlloySmelter.MOD_ID);

    public static final Supplier<RecipeSerializer<?>> SMELTING =
            RECIPE_SERIALIZERS.register("smelting", SmeltingRecipe.Serializer::new);
}