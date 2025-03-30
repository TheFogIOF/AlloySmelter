package sk.alloy_smelter.screen;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public abstract class IESlot extends Slot {

    public IESlot(Container inv, int id, int x, int y)
    {
        super(inv, id, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack)
    {
        return true;
    }

    public static class IEOutputSlot extends SlotItemHandlerIE
    {
        public IEOutputSlot(IItemHandler inv, int id, int x, int y)
        {
            super(inv, id, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack)
        {
            return false;
        }
    }

    public static class IEFuelSlot extends SlotItemHandlerIE
    {
        private final ForgeControllerMenu container;
        public IEFuelSlot(ForgeControllerMenu container, IItemHandler inv, int id, int x, int y)
        {
            super(inv, id, x, y);
            this.container = container;
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return container.isFuel(stack) || isBucket(stack);
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack)
        {
            return isBucket(stack)?1: super.getMaxStackSize(stack);
        }

        public static boolean isBucket(ItemStack stack)
        {
            return stack.getItem()== Items.BUCKET;
        }
    }

    private static abstract class SlotItemHandlerIE extends SlotItemHandler
    {
        public SlotItemHandlerIE(IItemHandler itemHandler, int index, int xPosition, int yPosition)
        {
            super(itemHandler, index, xPosition, yPosition);
        }
        @Override
        public int getMaxStackSize(@NotNull ItemStack stack)
        {
            return Math.min(Math.min(this.getMaxStackSize(), stack.getMaxStackSize()), super.getMaxStackSize(stack));
        }
    }
}
