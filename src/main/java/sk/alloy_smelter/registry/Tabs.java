package sk.alloy_smelter.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.recipe.SmeltingRecipe;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Tabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AlloySmelter.MOD_ID);

    public static final Supplier<CreativeModeTab> ALLOY_SMELTER_TAB = REGISTRY.register("alloy_smelter", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + AlloySmelter.MOD_ID + ".alloy_smelter"))
            .icon(() -> new ItemStack(Blocks.FORGE_CONTROLLER_TIER1.get()))
            .displayItems((params, output) -> {
                output.accept(Blocks.FORGE_CONTROLLER_TIER1.get());
                output.accept(net.minecraft.world.level.block.Blocks.BRICKS);
                output.accept(Blocks.FORGE_CONTROLLER_TIER2.get());
                output.accept(net.minecraft.world.level.block.Blocks.POLISHED_BLACKSTONE_BRICKS);
                output.accept(Blocks.FORGE_CONTROLLER_TIER3.get());
                output.accept(net.minecraft.world.level.block.Blocks.END_STONE_BRICKS);
            })
            .build()
    );

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
