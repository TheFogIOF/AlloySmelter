package sk.alloy_smelter;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static ModConfigSpec COMMON_CONFIG;

    public static final String CATEGORY_SETTINGS = "settings";
    public static ModConfigSpec.BooleanValue ENABLE_PASSIVE_FUEL_CONSUMPTION;

    static {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

        COMMON_BUILDER.comment("Settings").push(CATEGORY_SETTINGS);
        ENABLE_PASSIVE_FUEL_CONSUMPTION = COMMON_BUILDER.comment("Enable passive fuel consumption as a unit per tick.")
                .define("enablePassiveFuelConsumption", true);

        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
