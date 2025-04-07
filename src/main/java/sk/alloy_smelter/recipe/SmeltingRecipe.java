package sk.alloy_smelter.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.registry.RecipeTypes;
import sk.alloy_smelter.registry.RecipeSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SmeltingRecipe implements Recipe<CustomRecipeWrapper> {
    private final NonNullList<Material> inputItems;
    private final ItemStack output;
    private final int smeltingTime;
    private final int fuelPerTick;
    private final int requiredTier;
    private PlacementInfo info;

    public SmeltingRecipe(NonNullList<Material> inputItems, ItemStack output, int smeltingTime, int fuelPerTick, int requiredTier) {
        this.inputItems = inputItems;
        this.output = output;
        this.smeltingTime = smeltingTime;
        this.fuelPerTick = fuelPerTick;
        this.requiredTier = requiredTier;
    }

    public int getSmeltingTime() { return this.smeltingTime; }

    public int fuelPerTick() { return this.fuelPerTick; }

    public int getRequiredTier() { return this.requiredTier; }

    public ItemStack getOutput() { return getResultItem(null); }

    public NonNullList<Material> getMaterials() { return this.inputItems; }

    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output.copy();
    }

    @Override
    public ItemStack assemble(CustomRecipeWrapper recipeWrapper, HolderLookup.Provider provider) {
        return this.output.copy();
    }

    @Override
    public boolean matches(CustomRecipeWrapper recipeWrapper, Level level) {
        java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
        int i = 0;

        for (int j = 0; j < 2; ++j) {
            ItemStack itemstack = recipeWrapper.getItem(j);
            if (!itemstack.isEmpty()) {
                ++i;
                inputs.add(itemstack);
            }
        }

        return recipeWrapper.getTier() == this.getRequiredTier() && i == this.inputItems.size() && RecipeMatcher.findMatches(inputs, this.inputItems) != null;
    }

    @Override
    public RecipeSerializer<? extends Recipe<CustomRecipeWrapper>> getSerializer() {
        return (RecipeSerializer<? extends Recipe<CustomRecipeWrapper>>) RecipeSerializers.SMELTING.get();
    }

    @Override
    public RecipeType<? extends Recipe<CustomRecipeWrapper>> getType() {
        return RecipeTypes.SMELTING.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.info == null) {
            List<Ingredient> ingredients = new ArrayList<>();
            for (int i = 0; i < this.inputItems.size(); i++) {
                ingredients.add(this.inputItems.get(i).ingredient);
            }
            this.info = PlacementInfo.create(ingredients);
        }
        return this.info;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }

    public static class Serializer implements RecipeSerializer<SmeltingRecipe> {
        private static final MapCodec<SmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                NonNullList.codecOf(Material.CODEC).fieldOf("ingredients").forGetter(SmeltingRecipe::getMaterials),
                ItemStack.CODEC.fieldOf("result").forGetter(SmeltingRecipe::getOutput),
                Codec.INT.optionalFieldOf("smeltingTime", 200).forGetter(SmeltingRecipe::getSmeltingTime),
                Codec.INT.optionalFieldOf("fuelPerTick", 1).forGetter(SmeltingRecipe::fuelPerTick),
                Codec.INT.optionalFieldOf("requiredTier", 1).forGetter(SmeltingRecipe::getRequiredTier)
        ).apply(inst, SmeltingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SmeltingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.collection(NonNullList::createWithCapacity, Material.STREAM_CODEC), SmeltingRecipe::getMaterials,
                        ItemStack.STREAM_CODEC, SmeltingRecipe::getOutput,
                        ByteBufCodecs.INT, SmeltingRecipe::getSmeltingTime,
                        ByteBufCodecs.INT, SmeltingRecipe::fuelPerTick,
                        ByteBufCodecs.INT, SmeltingRecipe::getRequiredTier,
                        SmeltingRecipe::new
                );

        @Override
        public MapCodec<SmeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public static record Material(Ingredient ingredient, int count) implements Predicate<ItemStack>
    {
        public static final Codec<Material> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(Material::ingredient),
                Codec.INT.fieldOf("count").forGetter(Material::count)
        ).apply(instance, Material::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Material> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, Material::ingredient,
                ByteBufCodecs.INT, Material::count,
                Material::new
        );

        public static Material of(Ingredient ingredient, int count)
        {
            return new Material(ingredient, count);
        }

        @Override
        public boolean test(ItemStack itemStack)
        {
            return ingredient.test(itemStack) && itemStack.getCount() >= count;
        }
    }
}