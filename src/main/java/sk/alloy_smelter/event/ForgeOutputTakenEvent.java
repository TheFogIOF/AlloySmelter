package sk.alloy_smelter.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ForgeOutputTakenEvent extends Event {
    private final Player player;
    private final ItemStack output;

    public ForgeOutputTakenEvent(Player player, ItemStack output) {
        this.player = player;
        this.output = output;
    }

    public Player getEntity() { return player; }
    public ItemStack getOutput() { return output; }
}
