package sk.alloy_smelter.integration.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.registry.Blocks;
import sk.alloy_smelter.screen.ForgeControllerMenu;

import java.util.Arrays;
import java.util.List;

public class SmeltingRecipeCategory implements IRecipeCategory<SmeltingRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, "smelting");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID,
            "textures/gui/alloy_smelter.png");

    public static final RecipeType<SmeltingRecipe> SMELTING_RECIPE_TYPE = new RecipeType<>(UID, SmeltingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated flame;
    private final IDrawableAnimated arrow;

    public SmeltingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.FORGE_CONTROLLER.get()));
        flame = helper.drawableBuilder(TEXTURE, 177, 0, 14, 14).buildAnimated(200, IDrawableAnimated.StartDirection.TOP, true);
        arrow = helper.drawableBuilder(TEXTURE,176, 14, 24, 17).buildAnimated(200,IDrawableAnimated.StartDirection.LEFT,false);
    }

    @Override
    public RecipeType<SmeltingRecipe> getRecipeType() {
        return SMELTING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.alloy_smelter.alloy_smelter_controller");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }


    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SmeltingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 45).addItemStack(new ItemStack(Items.COAL));
        builder.addSlot(RecipeIngredientRole.INPUT, 62, 25).addIngredients(recipe.getMaterials().get(0).ingredient());
        if (recipe.getMaterials().size() > 1) {
            Ingredient ingredient = Ingredient.EMPTY;
            for (int i = 0; i < recipe.getMaterials().get(1).ingredient().getItems().length; i++)
            {
                ItemStack[] itemStacks = new ItemStack[recipe.getMaterials().get(1).ingredient().getItems().length];
                itemStacks[i] = recipe.getMaterials().get(1).ingredient().getItems()[i];
                itemStacks[i].setCount(recipe.getMaterials().get(1).count());
                ingredient = Ingredient.of(itemStacks);
            }
            builder.addSlot(RecipeIngredientRole.INPUT, 62, 45).addIngredients(ingredient);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 35).addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(SmeltingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        flame.draw(guiGraphics, 42, 46);
        arrow.draw(guiGraphics, 83, 35);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5f,0.5f,0.5f);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, recipe.getSmeltingTime()/20 + " ⌚/s", 189, 110, 0xFFFFFF);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, recipe.fuelPerTick() + " \uD83D\uDD25/tick", 56, 130, 0xFFFFFF);
        guiGraphics.pose().popPose();
    }
}
