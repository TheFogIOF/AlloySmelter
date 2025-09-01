package sk.alloy_smelter;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import sk.alloy_smelter.registry.*;
import sk.alloy_smelter.screen.ForgeControllerScreen;

@Mod(AlloySmelter.MOD_ID)
public class AlloySmelter {
    public static final String MOD_ID = "alloy_smelter";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AlloySmelter() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Items.register(modEventBus);
        Blocks.register(modEventBus);
        BlockEntities.register(modEventBus);
        MenuTypes.register(modEventBus);
        Recipes.register(modEventBus);
        Tabs.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(MenuTypes.FORGE_CONTROLLER_MENU.get(), ForgeControllerScreen::new);
        }
    }
}
