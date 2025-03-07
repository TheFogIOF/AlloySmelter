package sk.alloy_smelter.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.registry.RecipeTypes;
import sk.alloy_smelter.registry.RecipeSerializers;

import java.util.Optional;
import java.util.function.Predicate;

public class SmeltingRecipe implements Recipe<RecipeWrapper> {
    private final NonNullList<Material> inputItems;
    private final ItemStack output;
    private final int smeltingTime;
    private final int fuelPerTick;

    public SmeltingRecipe(NonNullList<Material> inputItems, ItemStack output, int smeltingTime, int fuelPerTick) {
        this.inputItems = inputItems;
        this.output = output;
        this.smeltingTime = smeltingTime;
        this.fuelPerTick = fuelPerTick;
    }

    public int getSmeltingTime() {
        return this.smeltingTime;
    }

    public int fuelPerTick() {
        return this.fuelPerTick;
    }

    public ItemStack getOutput() {
        return getResultItem(null);
    }

    public NonNullList<Material> getMaterials() {
        return this.inputItems;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output.copy();
    }

    @Override
    public ItemStack assemble(RecipeWrapper recipeWrapper, HolderLookup.Provider provider) {
        return this.output.copy();
    }

    @Override
    public boolean matches(RecipeWrapper recipeWrapper, Level level) {
        java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
        int i = 0;

        for (int j = 0; j < 2; ++j) {
            ItemStack itemstack = recipeWrapper.getItem(j);
            if (!itemstack.isEmpty()) {
                ++i;
                inputs.add(itemstack);
            }
        }
        return i == this.inputItems.size() && RecipeMatcher.findMatches(inputs, this.inputItems) != null;
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializers.SMELTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypes.SMELTING.get();
    }

    public static class Serializer implements RecipeSerializer<SmeltingRecipe> {
        private static final MapCodec<SmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                NonNullList.codecOf(Material.CODEC).fieldOf("ingredients").forGetter(SmeltingRecipe::getMaterials),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.output),
                Codec.INT.optionalFieldOf("smeltingTime", 200).forGetter(SmeltingRecipe::getSmeltingTime),
                Codec.INT.optionalFieldOf("fuelPerTick", 1).forGetter(SmeltingRecipe::fuelPerTick)
        ).apply(inst, SmeltingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SmeltingRecipe> STREAM_CODEC = StreamCodec.of(SmeltingRecipe.Serializer::toNetwork, SmeltingRecipe.Serializer::fromNetwork);

        @Override
        public MapCodec<SmeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static SmeltingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            int i = buffer.readVarInt();
            NonNullList<Material> inputItems = NonNullList.withSize(i, Material.of(Ingredient.EMPTY, 0));
            inputItems.replaceAll(ignored -> Material.STREAM_CODEC.decode(buffer));
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            int smeltingTime = buffer.readVarInt();
            int fuelPerTick = buffer.readVarInt();
            return new SmeltingRecipe(inputItems, output, smeltingTime, fuelPerTick);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, SmeltingRecipe recipe)
        {
            buffer.writeVarInt(recipe.inputItems.size());
            for (Material material : recipe.inputItems) Material.STREAM_CODEC.encode(buffer, material);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
            buffer.writeVarInt(recipe.smeltingTime);
            buffer.writeVarInt(recipe.fuelPerTick);
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