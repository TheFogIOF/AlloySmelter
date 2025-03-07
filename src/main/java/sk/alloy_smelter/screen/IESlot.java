package sk.alloy_smelter.screen;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public abstract class IESlot extends Slot {
    final AbstractContainerMenu containerMenu;

    public IESlot(AbstractContainerMenu containerMenu, Container inv, int id, int x, int y)
    {
        super(inv, id, x, y);
        this.containerMenu = containerMenu;
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
        public IEFuelSlot(IItemHandler inv, int id, int x, int y)
        {
            super(inv, id, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return AbstractFurnaceBlockEntity.isFuel(stack)||isBucket(stack);
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

    private static class SlotItemHandlerIE extends SlotItemHandler
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
