package sk.alloy_smelter.integration.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import sk.alloy_smelter.recipe.SmeltingRecipe;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SmeltingDisplay extends AbstractCustomDisplay {
    public static DisplaySerializer<SmeltingDisplay> SERIALIZER = serializer(SmeltingDisplay::new);

    public SmeltingDisplay(RecipeHolder<SmeltingRecipe> recipe) {
        super(recipe);
    }

    public SmeltingDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> id, int smeltingTime, int fuelPerTick) {
        super(input, output, id, smeltingTime, fuelPerTick);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return SmeltingCategoryREI.SMELTING_RECIPE_TYPE;
    }

    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
