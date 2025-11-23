package sk.alloy_smelter;

import com.mojang.logging.LogUtils;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import sk.alloy_smelter.event.ForgeOutputTakenEvent;
import sk.alloy_smelter.registry.*;
import sk.alloy_smelter.screen.ForgeControllerMenu;
import sk.alloy_smelter.screen.ForgeControllerScreen;

@Mod(AlloySmelter.MOD_ID)
public class AlloySmelter {
    public static final String MOD_ID = "alloy_smelter";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AlloySmelter(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

        Items.register(modEventBus);
        Blocks.register(modEventBus);
        BlockEntities.register(modEventBus);
        MenuTypes.register(modEventBus);
        RecipeTypes.RECIPE_TYPES.register(modEventBus);
        RecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        Tabs.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(MenuTypes.FORGE_CONTROLLER_MENU.get(), ForgeControllerScreen::new);
        }
    }
}
