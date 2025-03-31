package sk.alloy_smelter.screen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTypes;
import sk.alloy_smelter.screen.IESlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import sk.alloy_smelter.block.ForgeControllerBlockEntity;
import sk.alloy_smelter.registry.Blocks;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.registry.MenuTypes;
import sk.alloy_smelter.registry.RecipeTypes;

import java.util.Optional;

import static sk.alloy_smelter.registry.Tags.*;

public class ForgeControllerMenu extends AbstractContainerMenu {

    public final ForgeControllerBlockEntity blockEntity;
    public final ItemStackHandler inventory;
    private final Level level;
    private final ContainerData data;

    public ForgeControllerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public ForgeControllerMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuTypes.FORGE_CONTROLLER_MENU.get(), pContainerId);
        checkContainerSize(inv, 4);
        this.blockEntity = ((ForgeControllerBlockEntity) entity);
        this.inventory = blockEntity.getInventory();
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new IESlot.IEFuelSlot(this, inventory, 0, 20, 45));
        this.addSlot(new SlotItemHandler(inventory, 1, 62, 25));
        this.addSlot(new SlotItemHandler(inventory, 2, 62, 45));
        this.addSlot(new IESlot.IEOutputSlot(inventory, 3, 120, 35));

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        int lastSlotIndex = 3;

        Slot sourceSlot = slots.get(index);
        int slotCount = lastSlotIndex + 1;
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();
        if (index < 36) {
            if (!moveItemStackTo(sourceStack, 36, 36 + slotCount, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 36 + slotCount) {
            if (!moveItemStackTo(sourceStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + index);
            return ItemStack.EMPTY;
        }
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    protected static boolean valid(ContainerLevelAccess access, Player player, TagKey<Block> targetBlock) {
        return (Boolean)access.evaluate((levelPosConsumer, defaultValue) -> {
            return !levelPosConsumer.getBlockState(defaultValue).is(targetBlock) ? false : player.canInteractWithBlock(defaultValue, 4.0);
        }, true);
    }

    @Override
    public boolean stillValid(Player player) {
        return valid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, FORGE_CONTROLLER);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) for (int l = 0; l < 9; ++l) this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
    }

    public boolean isFuel(ItemStack stack) { return stack.getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0; }

    public boolean isCrafting() { return data.get(0) > 0; }

    public boolean isBurning() { return data.get(2) > 0; }

    public int getArrowProgress() {
        int smeltingTime = this.data.get(0);
        int maxSmeltingTime = this.data.get(1);
        int progressSize = 26;

        return maxSmeltingTime != 0 && smeltingTime != 0 ? smeltingTime * progressSize / maxSmeltingTime : 0;
    }

    public int getBurnProgress() {
        int fuelTime = this.data.get(2);
        int maxFuelTime = this.data.get(3);
        int progressSize = 12;

        return maxFuelTime != 0 && fuelTime != 0 ? fuelTime * progressSize / maxFuelTime : 0;
    }
}