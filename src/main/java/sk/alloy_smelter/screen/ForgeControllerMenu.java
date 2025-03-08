package sk.alloy_smelter.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import sk.alloy_smelter.block.ForgeControllerBlockEntity;
import sk.alloy_smelter.registry.Blocks;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.registry.MenuTypes;

import java.util.Optional;

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

        this.addSlot(new IESlot.IEFuelSlot(inventory, 0, 20, 45));
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

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, Blocks.FORGE_CONTROLLER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public boolean isBurning() {
        return data.get(1) > 0;
    }

    public int getArrowProgress() {
        Optional<RecipeHolder<SmeltingRecipe>> recipe = blockEntity.getMatchingRecipe();

        int smeltingTime = this.data.get(0);
        int maxSmeltingTime = 0;
        if (recipe.isPresent()) maxSmeltingTime = recipe.get().value().getSmeltingTime();
        int progressSize = 26;

        return maxSmeltingTime != 0 && smeltingTime != 0 ? smeltingTime * progressSize / maxSmeltingTime : 0;
    }

    public int getBurnProgress() {
        int fuelTime = this.data.get(1);
        int maxFuelTime = this.data.get(2);
        int progressSize = 12;

        return maxFuelTime != 0 && fuelTime != 0 ? fuelTime * progressSize / maxFuelTime : 0;
    }
}