package sk.alloy_smelter.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import sk.alloy_smelter.AlloySmelter;

import javax.annotation.Nullable;

public class SmeltingRecipe implements Recipe<CustomRecipeWrapper> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputItems;
    private final ItemStack output;
    private final int smeltingTime;
    private final int fuelPerTick;
    private final int requiredTier;

    public SmeltingRecipe(ResourceLocation id, NonNullList<Ingredient> inputItems, ItemStack output, int smeltingTime, int fuelPerTick, int requiredTier) {
        this.id = id;
        this.inputItems = inputItems;
        this.output = output;
        this.smeltingTime = smeltingTime;
        this.fuelPerTick = fuelPerTick;
        this.requiredTier = requiredTier;
    }

    public int getSmeltingTime() {
        return this.smeltingTime;
    }

    public int fuelPerTick() {
        return this.fuelPerTick;
    }

    public int getRequiredTier()
    {
        return this.requiredTier;
    }

    public ItemStack getOutput() {
        return getResultItem(null);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputItems;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return this.output.copy();
    }

    @Override
    public ItemStack assemble(CustomRecipeWrapper recipeWrapper, RegistryAccess registryAccess) {
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

        return recipeWrapper.getTier() == this.getRequiredTier() && i == this.inputItems.size() && net.minecraftforge.common.util.RecipeMatcher.findMatches(inputs, this.inputItems) != null;
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<SmeltingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "smelting";
    }

    public static class Serializer implements RecipeSerializer<SmeltingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(AlloySmelter.MOD_ID, "smelting");

        @Override
        public SmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            NonNullList<Ingredient> inputItems = readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));

            if (inputItems.isEmpty()) throw new JsonParseException("No ingredients for recipe");
            else if (inputItems.size() > 2) throw new JsonParseException("Too many ingredients for recipe! The max is 2");
            else {
                ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
                int smeltingTime = GsonHelper.getAsInt(json, "smeltingTime", 200);
                int fuelPerTick = GsonHelper.getAsInt(json, "fuelPerTick", 1);
                int requiredTier = GsonHelper.getAsInt(json, "requiredTier", 1);

                return new SmeltingRecipe(id, inputItems, output, smeltingTime, fuelPerTick, requiredTier);
            }
        }

        private static NonNullList<Ingredient> readIngredients(JsonArray ingredientArray) {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();
            for (int i = 0; i < ingredientArray.size(); ++i) {
                JsonObject jsonIngredient = ingredientArray.get(i).getAsJsonObject();
                Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i));
                int count = 1; if (jsonIngredient.has("count")) count = GsonHelper.getAsInt(jsonIngredient, "count");
                ingredient.getItems()[0].setCount(count);
                if (!ingredient.isEmpty()) nonnulllist.add(ingredient);
            }
            return nonnulllist;
        }

        @Override
        public @Nullable SmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int i = buffer.readVarInt();
            NonNullList<Ingredient> inputItems = NonNullList.withSize(i, Ingredient.EMPTY);
            for (int j = 0; j < inputItems.size(); ++j) {
                inputItems.set(j, Ingredient.fromNetwork(buffer));
            }
            ItemStack output = buffer.readItem();
            int smeltingTime = buffer.readVarInt();
            int fuelPerTick = buffer.readVarInt();
            int requiredTier = buffer.readVarInt();
            return new SmeltingRecipe(id, inputItems, output, smeltingTime, fuelPerTick, requiredTier);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SmeltingRecipe recipe)
        {
            buffer.writeVarInt(recipe.inputItems.size());
            for (Ingredient ingredient : recipe.inputItems) ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.smeltingTime);
            buffer.writeVarInt(recipe.fuelPerTick);
            buffer.writeVarInt(recipe.requiredTier);
        }
    }
}
