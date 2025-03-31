package sk.alloy_smelter.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class CustomRecipeWrapper extends RecipeWrapper implements RecipeInput {
    protected final IItemHandler inv;
    protected final int tier;

    public CustomRecipeWrapper(IItemHandler inv, int tier) {
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
