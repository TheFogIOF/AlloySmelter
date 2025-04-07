package sk.alloy_smelter.integration.rei;

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.forge.REIPluginCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.integration.rei.smelting.DisplaySmeltingT1;
import sk.alloy_smelter.integration.rei.smelting.DisplaySmeltingT2;
import sk.alloy_smelter.integration.rei.smelting.DisplaySmeltingT3;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.registry.RecipeTypes;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@REIPluginCommon
public class REIServerPlugin implements REICommonPlugin {
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, "rei_smelting_tier1"), DisplaySmeltingT1.SERIALIZER);
        registry.register(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, "rei_smelting_tier2"), DisplaySmeltingT2.SERIALIZER);
        registry.register(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, "rei_smelting_tier3"), DisplaySmeltingT3.SERIALIZER);
    }

    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getServer().getRecipeManager();
        Stream<RecipeHolder<?>> allRecipes = recipeManager.getRecipes().stream();
        List<RecipeHolder<SmeltingRecipe>> smeltingRecipes = new ArrayList<>();
        allRecipes.filter(recipe -> recipe.value().getType() == RecipeTypes.SMELTING.get()).forEach(r -> smeltingRecipes.add((RecipeHolder<SmeltingRecipe>) r));
        smeltingRecipes.forEach(recipe -> {
            if (recipe.value().getRequiredTier() == 1) registry.add(new DisplaySmeltingT1(recipe), recipe);
            if (recipe.value().getRequiredTier() == 2) registry.add(new DisplaySmeltingT2(recipe), recipe);
            if (recipe.value().getRequiredTier() == 3) registry.add(new DisplaySmeltingT3(recipe), recipe);
        });
    }
}
