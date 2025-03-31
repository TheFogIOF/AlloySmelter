package sk.alloy_smelter.integration.rei.smelting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import sk.alloy_smelter.recipe.SmeltingRecipe;

import java.util.*;

public abstract class AbstractCustomDisplay extends BasicDisplay {
    protected int smeltingTime;
    protected int fuelPerTick;
    protected int forgeTier;

    public AbstractCustomDisplay(RecipeHolder<? extends SmeltingRecipe> recipe) {
        this(
                getInput(recipe.value()),
                getOutput(recipe.value()),
                recipe.value().getSmeltingTime(),
                recipe.value().fuelPerTick(),
                recipe.value().getRequiredTier()
        );
    }

    private static List<EntryIngredient> getInput(SmeltingRecipe recipe) {
        if (recipe == null) return Collections.emptyList();
        List<EntryIngredient> list = new ArrayList<>();

        for (SmeltingRecipe.Material material : recipe.getMaterials()) {
            List<ItemStack> itemStacks = new ArrayList<>(material.ingredient().getItems().length);
            for (int i = 0; i < material.ingredient().getItems().length; i++) {
                itemStacks.add(new ItemStack(material.ingredient().getItems()[i].getItem()).copyWithCount(material.count()));
            }
            list.add(EntryIngredients.ofItemStacks(itemStacks));
        }

        return list;
    }
    private static List<EntryIngredient> getOutput(SmeltingRecipe recipe) {
        if (recipe == null) return Collections.emptyList();
        List<EntryIngredient> list = new ArrayList<>();
        list.add(EntryIngredient.of(EntryStacks.of(recipe.getOutput())));
        return list;
    }

    public AbstractCustomDisplay(List<EntryIngredient> input, List<EntryIngredient> output, int smeltingTime, int fuelPerTick, int forgeTier) {
        super(input, output);
        this.smeltingTime = smeltingTime;
        this.fuelPerTick = fuelPerTick;
        this.forgeTier = forgeTier;
    }
}
