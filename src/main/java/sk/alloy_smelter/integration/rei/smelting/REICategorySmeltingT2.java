package sk.alloy_smelter.integration.rei.smelting;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.registry.Blocks;

import java.util.LinkedList;
import java.util.List;

public class REICategorySmeltingT2 implements DisplayCategory<DisplaySmeltingT2> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID,
            "textures/gui/jei_alloy_smelter.png");

    public static final CategoryIdentifier<DisplaySmeltingT2> SMELTING_RECIPE_TYPE =
            CategoryIdentifier.of(AlloySmelter.MOD_ID, "smelting_tier2");

    @Override
    public CategoryIdentifier<? extends DisplaySmeltingT2> getCategoryIdentifier() {
        return SMELTING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("rei.alloy_smelter.forge_controller_tier2");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Blocks.FORGE_CONTROLLER_TIER2.get());
    }

    @Override
    public List<Widget> setupDisplay(DisplaySmeltingT2 display, Rectangle bounds) {
        final Point startPoint = new Point(bounds.getCenterX() - 88, bounds.getCenterY() - 42);
        List<Widget> widgets = new LinkedList<>();
        widgets.add(Widgets.createCategoryBase(bounds));

        widgets.add(Widgets.createTexturedWidget(TEXTURE, new Rectangle(startPoint.x, startPoint.y, 175, 82)));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 20, startPoint.y + 45)).disableBackground()
                .entries(EntryIngredient.of(EntryStacks.of(Items.COAL))));

        EntryIngredient entryIngredient_0 = display.getInputEntries().get(0);

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 62, startPoint.y + 25)).disableBackground()
                .entries(entryIngredient_0));

        EntryIngredient entryIngredient_1 = EntryIngredient.empty();
        if (display.getInputEntries().size() > 1) {
            entryIngredient_1 = display.getInputEntries().get(1);
        }
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 62, startPoint.y + 45)).disableBackground()
                .entries(entryIngredient_1));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 120, startPoint.y + 35)).disableBackground()
                .markOutput().entries(display.getOutputEntries().getFirst()));

        widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            int height = 12 - Mth.ceil((System.currentTimeMillis() / (3200 / 12) % 12d));
            graphics.blit(TEXTURE, startPoint.x + 44, startPoint.y + 47 + 12 - height, 179, 1 + (12 - height), 9, height, 256, 256);
        }));

        widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            int width = Mth.ceil((System.currentTimeMillis() / (3200 / 22) % 22d));
            graphics.blit(TEXTURE, startPoint.x + 84, startPoint.y + 35, 177, 14, width, 16, 256, 256);
        }));

        widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            graphics.pose().pushPose();
            graphics.pose().scale(0.5f, 0.5f, 0.5f);
            graphics.drawCenteredString(Minecraft.getInstance().font, display.smeltingTime / 20 + " ⌚/s", startPoint.x * 2 + 189, startPoint.y * 2 + 110, 0xFFFFFF);
            graphics.drawCenteredString(Minecraft.getInstance().font, display.fuelPerTick + " \uD83D\uDD25/tick", startPoint.x * 2 + 56, startPoint.y * 2 + 130, 0xFFFFFF);
            graphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.alloy_smelter.forge_tier").getString() + " " + display.forgeTier, startPoint.x * 2 + 16, startPoint.y * 2 + 12, 0xFFFFFF);
            graphics.pose().popPose();
        }));

        return widgets;
    }

    @Override
    public int getDisplayWidth(DisplaySmeltingT2 smeltingDisplay) {
        return 184;
    }

    @Override
    public int getDisplayHeight() {
        return 90;
    }
}
