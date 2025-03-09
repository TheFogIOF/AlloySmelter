package sk.alloy_smelter.integration.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.registry.Blocks;

import java.util.*;
import java.util.List;

public class SmeltingCategoryREI implements DisplayCategory<SmeltingDisplay> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID,
            "textures/gui/alloy_smelter.png");

    public static final CategoryIdentifier<SmeltingDisplay> SMELTING_RECIPE_TYPE =
            CategoryIdentifier.of(AlloySmelter.MOD_ID, "smelting");

    @Override
    public CategoryIdentifier<? extends SmeltingDisplay> getCategoryIdentifier() {
        return SMELTING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.alloy_smelter.alloy_smelter_controller");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Blocks.FORGE_CONTROLLER.get());
    }

    @Override
    public List<Widget> setupDisplay(SmeltingDisplay display, Rectangle bounds) {
        final Point startPoint = new Point(bounds.getCenterX() - 88, bounds.getCenterY() - 42);
        List<Widget> widgets = new LinkedList<>();
        SmeltingRecipe recipe = display.smeltingRecipe;

        widgets.add(Widgets.createCategoryBase(bounds));

        widgets.add(Widgets.createTexturedWidget(TEXTURE, new Rectangle(startPoint.x, startPoint.y, 175, 82)));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 20,startPoint.y + 45)).disableBackground()
                .entries(EntryIngredient.of(EntryStacks.of(Items.COAL))));

        EntryIngredient entryIngredient_0 = EntryIngredient.empty();
        for (int i = 0; i < recipe.getMaterials().get(0).ingredient().getItems().length; i++) {
            ItemStack[] itemStacks = new ItemStack[recipe.getMaterials().get(0).ingredient().getItems().length];
            itemStacks[i] = recipe.getMaterials().get(0).ingredient().getItems()[i];
            itemStacks[i].setCount(recipe.getMaterials().get(0).count());
            entryIngredient_0 = EntryIngredients.ofIngredient(Ingredient.of(itemStacks));
        }
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 62,startPoint.y + 25)).disableBackground()
                .entries(entryIngredient_0));

        EntryIngredient entryIngredient_1 = EntryIngredient.empty();
        if (recipe.getMaterials().size() > 1) {
            for (int i = 0; i < recipe.getMaterials().get(1).ingredient().getItems().length; i++) {
                ItemStack[] itemStacks = new ItemStack[recipe.getMaterials().get(1).ingredient().getItems().length];
                itemStacks[i] = recipe.getMaterials().get(1).ingredient().getItems()[i];
                itemStacks[i].setCount(recipe.getMaterials().get(1).count());
                entryIngredient_1 = EntryIngredients.ofIngredient(Ingredient.of(itemStacks));
            }
        }
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 62,startPoint.y + 45)).disableBackground()
                .entries(entryIngredient_1));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 120,startPoint.y + 35)).disableBackground()
                .markOutput().entries(display.getOutputEntries().getFirst()));

        widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            int height = 12 - Mth.ceil((System.currentTimeMillis() / (3200 / 12) % 12d));
            graphics.blit(TEXTURE, startPoint.x + 44,startPoint.y + 47 + 12 - height,179,1 + (12 - height),9, height);
        }));

        widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            int width = Mth.ceil((System.currentTimeMillis() / (3200 / 22) % 22d));
            graphics.blit(TEXTURE, startPoint.x + 84, startPoint.y + 35,177,14, width, 16);
        }));

        widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            graphics.pose().pushPose();
            graphics.pose().scale(0.5f,0.5f,0.5f);
            graphics.drawCenteredString(Minecraft.getInstance().font, display.smeltingRecipe.getSmeltingTime()/20 + " ⌚/s", startPoint.x * 2 + 189, startPoint.y * 2 + 110, 0xFFFFFF);
            graphics.drawCenteredString(Minecraft.getInstance().font, display.smeltingRecipe.fuelPerTick() + " \uD83D\uDD25/tick", startPoint.x * 2 + 56, startPoint.y * 2 + 130, 0xFFFFFF);
            graphics.pose().popPose();
        }));

        return widgets;
    }

    @Override
    public int getDisplayWidth(SmeltingDisplay smeltingDisplay) {
        return 184;
    }

    @Override
    public int getDisplayHeight() {
        return 90;
    }
}
