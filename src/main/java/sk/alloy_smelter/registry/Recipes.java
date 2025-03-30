package sk.alloy_smelter.registry;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.recipe.SmeltingRecipe;

public class Recipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AlloySmelter.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SmeltingRecipe>> SMELTING =
            SERIALIZERS.register("smelting", () -> SmeltingRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}