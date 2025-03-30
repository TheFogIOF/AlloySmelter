package sk.alloy_smelter.integration.rei;

import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.forge.REIPluginCommon;
import net.minecraft.resources.ResourceLocation;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.registry.RecipeTypes;

import java.lang.annotation.Annotation;

@REIPluginCommon
public class REIServerPlugin implements REICommonPlugin {
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, "rei_smelting"), SmeltingDisplay.SERIALIZER);
    }

    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(SmeltingRecipe.class).filterType(RecipeTypes.SMELTING.get()).fill(SmeltingDisplay::new);
    }
}
