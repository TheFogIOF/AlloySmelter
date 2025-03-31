package sk.alloy_smelter.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class CustomRecipeWrapper extends RecipeWrapper {
    protected final IItemHandlerModifiable inv;
    protected final int tier;

    public CustomRecipeWrapper(IItemHandlerModifiable inv, int tier) {
        super(inv);
        this.inv = inv;
        this.tier = tier;
    }

    public int size() {
        return this.inv.getSlots();
    }

    public ItemStack getItem(int slot) {
        return this.inv.getStackInSlot(slot);
    }

    public int getTier() { return this.tier; }
}
