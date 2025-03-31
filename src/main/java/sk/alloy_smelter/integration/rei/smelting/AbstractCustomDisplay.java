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
                Optional.of(recipe.id().location()),
                recipe.value().getSmeltingTime(),
                recipe.value().fuelPerTick(),
                recipe.value().getRequiredTier()
        );
    }

    public AbstractCustomDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> id, CompoundTag tag) {
        this(input, output, id, tag.getInt("smeltingTime"), tag.getInt("fuelPerTick"), tag.getInt("requiredTier"));
    }

    private static List<EntryIngredient> getInput(SmeltingRecipe recipe) {
        if (recipe == null) return Collections.emptyList();
        List<EntryIngredient> list = new ArrayList<>();

        for (sk.alloy_smelter.recipe.SmeltingRecipe.Material material : recipe.getMaterials()) {
            List<ItemStack> itemStacks = new ArrayList<>(material.ingredient().getValues().size());
            for (int i = 0; i < material.ingredient().getValues().size(); i++) {
                itemStacks.add(new ItemStack(material.ingredient().getValues().get(i)).copyWithCount(material.count()));
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

    public OptionalInt smeltingTime() {
        return OptionalInt.of(smeltingTime);
    }

    public OptionalInt fuelPerTick() {
        return OptionalInt.of(fuelPerTick);
    }

    public OptionalInt forgeTier() {
        return OptionalInt.of(forgeTier);
    }

    public AbstractCustomDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location, int smeltingTime, int fuelPerTick, int forgeTier) {
        super(input, output, location);
        this.smeltingTime = smeltingTime;
        this.fuelPerTick = fuelPerTick;
        this.forgeTier = forgeTier;
    }

    protected static <D extends AbstractCustomDisplay> DisplaySerializer<D> serializer(Constructor<D> constructor) {
        return DisplaySerializer.of(
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(D::getInputEntries),
                        EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(D::getOutputEntries),
                        ResourceLocation.CODEC.optionalFieldOf("location").forGetter(D::getDisplayLocation),
                        Codec.INT.fieldOf("smeltingTime").forGetter(display -> display.smeltingTime),
                        Codec.INT.fieldOf("fuelPerTick").forGetter(display -> display.fuelPerTick),
                        Codec.INT.fieldOf("forgeTier").forGetter(display -> display.forgeTier)
                ).apply(instance, constructor::create)),
                StreamCodec.composite(
                        EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                        D::getInputEntries,
                        EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                        D::getOutputEntries,
                        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                        D::getDisplayLocation,
                        ByteBufCodecs.INT,
                        display -> display.smeltingTime,
                        ByteBufCodecs.INT,
                        display -> display.fuelPerTick,
                        ByteBufCodecs.INT,
                        display -> display.forgeTier,
                        constructor::create
                ));
    }

    protected interface Constructor<T extends AbstractCustomDisplay> {
        T create(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location, int smeltingTime, int fuelPerTick, int forgeTier);
    }
}
