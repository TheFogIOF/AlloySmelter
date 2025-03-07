package sk.alloy_smelter.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.recipe.SmeltingRecipe;

public class Tabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AlloySmelter.MOD_ID);

    @SubscribeEvent
    public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
        if (tabData.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            tabData.accept(Blocks.FORGE_CONTROLLER.get().asItem());
        }
    }

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
