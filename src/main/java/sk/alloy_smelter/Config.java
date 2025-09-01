package sk.alloy_smelter;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static ForgeConfigSpec COMMON_CONFIG;

    public static final String CATEGORY_SETTINGS = "settings";
    public static ForgeConfigSpec.BooleanValue ENABLE_PASSIVE_FUEL_CONSUMPTION;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        COMMON_BUILDER.comment("Settings").push(CATEGORY_SETTINGS);
        ENABLE_PASSIVE_FUEL_CONSUMPTION = COMMON_BUILDER.comment("Enable passive fuel consumption as a unit per tick.")
                .define("enablePassiveFuelConsumption", true);

        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
