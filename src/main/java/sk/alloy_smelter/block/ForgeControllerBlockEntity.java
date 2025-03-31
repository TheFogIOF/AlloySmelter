package sk.alloy_smelter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.recipe.CustomRecipeWrapper;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.registry.BlockEntities;
import sk.alloy_smelter.registry.Tags;
import sk.alloy_smelter.screen.ForgeControllerMenu;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ForgeControllerBlockEntity extends SyncedBlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            inventoryChanged();
        }
    };

    private static final int FUEL_SLOT = 0;
    private static final int[] INPUT_SLOTS = {1, 2};
    private static final int OUTPUT_SLOT = 3;

    private static final int FORGE_TIERS = 3;

    private final int tier;
    private final List<BlockPos> multiblockPositions;
    private final Direction facing;

    private LazyOptional<IItemHandler> playerItemHandler;
    private LazyOptional<IItemHandler>[] hopperItemHandler = new LazyOptional[4];

    protected final ContainerData data;

    private int smeltProgress;
    private int fuelTime;
    private int burnedFuelTime;

    public ForgeControllerBlockEntity(BlockPos position, BlockState state) {
        super(BlockEntities.FORGE_CONTROLLER_BLOCK_ENTITY.get(), position, state);
        facing = state.getValue(ForgeControllerBlock.FACING);
        multiblockPositions = generateMultiblock(position, state.getValue(ForgeControllerBlock.FACING));
        tier = ((ForgeControllerBlock) state.getBlock()).tier;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> ForgeControllerBlockEntity.this.smeltProgress;
                    case 1 -> ForgeControllerBlockEntity.this.fuelTime;
                    case 2 -> ForgeControllerBlockEntity.this.burnedFuelTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> ForgeControllerBlockEntity.this.smeltProgress = value;
                    case 1 -> ForgeControllerBlockEntity.this.fuelTime = value;
                    case 2 -> ForgeControllerBlockEntity.this.burnedFuelTime = value;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    private IItemHandler createHopperFrontItemHandler() {
        return new IItemHandler() {
            @Override
            public int getSlots() { return 1; }
            @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return itemHandler.insertItem(0, stack, simulate); }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
        };
    }
    private IItemHandler createHopperBottomItemHandler() {
        return new IItemHandler() {
            @Override public int getSlots() { return 1; }
            @Override public ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(3); }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; } // No insertion
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return itemHandler.extractItem(3, amount, simulate); }
            @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
        };
    }
    private IItemHandler createHopperLeftItemHandler() {
        return new IItemHandler() {
            @Override public int getSlots() { return 2; }
            @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return itemHandler.insertItem(1, stack, simulate); }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
        };
    }
    private IItemHandler createHopperRightItemHandler() {
        return new IItemHandler() {
            @Override public int getSlots() { return 2; }
            @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return itemHandler.insertItem(2, stack, simulate); }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
        };
    }
    private Direction getSide(Direction direction, int state) {
        Direction[] directions = new Direction[4];
        directions[0] = Direction.EAST;
        directions[1] = Direction.NORTH;
        directions[2] = Direction.WEST;
        directions[3] = Direction.SOUTH;
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == direction) {
                if (state == 0) return directions[i];
                if (state == 1) return (i == 0) ? directions[i + 3] : directions[i - 1];
                if (state == 2) return (i == 3) ? directions[i - 3] : directions[i + 1];
            }
        }
        return Direction.UP;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == this.facing) { return this.hopperItemHandler[0].cast(); }
            if (side == Direction.DOWN) { return this.hopperItemHandler[1].cast(); }
            if (getSide(this.facing, 1) == side) return this.hopperItemHandler[2].cast();
            if (getSide(this.facing, 2) == side) return this.hopperItemHandler[3].cast();
            return this.playerItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.playerItemHandler.invalidate();
        this.hopperItemHandler[0].invalidate();
        this.hopperItemHandler[1].invalidate();
        this.hopperItemHandler[2].invalidate();
        this.hopperItemHandler[3].invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.playerItemHandler = LazyOptional.of(() -> this.itemHandler);
        this.hopperItemHandler[0] = LazyOptional.of(() -> createHopperFrontItemHandler());
        this.hopperItemHandler[1] = LazyOptional.of(() -> createHopperBottomItemHandler());
        this.hopperItemHandler[2] = LazyOptional.of(() -> createHopperLeftItemHandler());
        this.hopperItemHandler[3] = LazyOptional.of(() -> createHopperRightItemHandler());
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.alloy_smelter.alloy_smelter_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ForgeControllerMenu(i, inventory, this, this.data);
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.put("inventory", itemHandler.serializeNBT());
        compoundTag.putInt("smeltProgress", smeltProgress);
        compoundTag.putInt("fuelTime", fuelTime);
        compoundTag.putInt("burnedFuelTime", burnedFuelTime);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        smeltProgress = compoundTag.getInt("smeltProgress");
        fuelTime = compoundTag.getInt("fuelTime");
        burnedFuelTime = compoundTag.getInt("burnedFuelTime");
        itemHandler.deserializeNBT(compoundTag.getCompound("inventory"));
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, ForgeControllerBlockEntity forgeController) {
        if (!forgeController.verifyMultiblock()) return;
        if (!blockState.getValue(ForgeControllerBlock.LIT)) return;
        if (level.random.nextDouble() > 0.8) {
            double randH = -0.1 + (0.1 + 0.1) * level.random.nextDouble();
            double randW = -0.25 + (0.25 + 0.25) * level.random.nextDouble();
            switch (forgeController.facing) {
                case EAST -> {
                    level.addParticle(ParticleTypes.FLAME, blockPos.getX() + 1.05, blockPos.getY() + 0.7 + randH, blockPos.getZ() + 0.5 + randW, 0, 0, 0);
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, blockPos.getX() - 0.5 + randW, blockPos.getY() + 0.5 + randW, blockPos.getZ() + 0.5 + randW, 0, 0.05, 0);
                }
                case WEST -> {
                    level.addParticle(ParticleTypes.FLAME, blockPos.getX() - 0.05, blockPos.getY() + 0.7 + randH, blockPos.getZ() + 0.5 + randW, 0, 0, 0);
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, blockPos.getX() + 1.5 + randW, blockPos.getY() + 0.5 + randW, blockPos.getZ() + 0.5 + randW, 0, 0.05, 0);
                }
                case NORTH -> {
                    level.addParticle(ParticleTypes.FLAME, blockPos.getX() + 0.5 + randW, blockPos.getY() + 0.7 + randH, blockPos.getZ() - 0.05, 0, 0, 0);
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, blockPos.getX() + 0.5 + randW, blockPos.getY() + 0.5 + randW, blockPos.getZ() + 1.5 + randW, 0, 0.05, 0);
                }
                case SOUTH -> {
                    level.addParticle(ParticleTypes.FLAME, blockPos.getX() + 0.5 + randW, blockPos.getY() + 0.7 + randH, blockPos.getZ() + 1.05, 0, 0, 0);
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, blockPos.getX() + 0.5 + randW, blockPos.getY() + 0.5 + randW, blockPos.getZ() - 0.5 + randW, 0, 0.05, 0);
                }
            }
        }
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, ForgeControllerBlockEntity forgeController) {
        if (forgeController.itemHandler.getStackInSlot(FUEL_SLOT).is(Tags.ALLOY_SMELTER_FUEL) && forgeController.fuelTime < 1) {
            forgeController.burnedFuelTime = ForgeHooks.getBurnTime(forgeController.itemHandler.getStackInSlot(FUEL_SLOT), RecipeType.SMELTING);
            forgeController.fuelTime += ForgeHooks.getBurnTime(forgeController.itemHandler.getStackInSlot(FUEL_SLOT), RecipeType.SMELTING);
            forgeController.itemHandler.getStackInSlot(FUEL_SLOT).shrink(1);
        }

        if (forgeController.fuelTime > 0 && forgeController.verifyMultiblock())
            level.setBlock(blockPos, blockState.setValue(ForgeControllerBlock.LIT, true), 3);
        else level.setBlock(blockPos, blockState.setValue(ForgeControllerBlock.LIT, false), 3);

        if (!forgeController.verifyMultiblock()) return;

        Optional<SmeltingRecipe> recipe = forgeController.getMatchingRecipe();
        if (recipe.isPresent() && forgeController.canSmelt(recipe.get())) {
            forgeController.smeltProgress++;
            if (forgeController.smeltProgress > recipe.get().getSmeltingTime()) {
                if (recipe.get().getIngredients().size() > 1) {
                    forgeController.itemHandler.getStackInSlot(INPUT_SLOTS[0]).shrink(recipe.get().getIngredients().get(0).getItems()[0].getCount());
                    forgeController.itemHandler.getStackInSlot(INPUT_SLOTS[1]).shrink(recipe.get().getIngredients().get(1).getItems()[0].getCount());
                } else {
                    forgeController.itemHandler.getStackInSlot(INPUT_SLOTS[0]).shrink(recipe.get().getIngredients().get(0).getItems()[0].getCount());
                    forgeController.itemHandler.getStackInSlot(INPUT_SLOTS[1]).shrink(recipe.get().getIngredients().get(0).getItems()[0].getCount());
                }
                forgeController.smeltProgress = 0;
                if (forgeController.itemHandler.getStackInSlot(OUTPUT_SLOT) == ItemStack.EMPTY) {
                    forgeController.itemHandler.setStackInSlot(OUTPUT_SLOT, recipe.get().getOutput());
                } else if (forgeController.itemHandler.getStackInSlot(OUTPUT_SLOT).getItem() == recipe.get().getOutput().getItem()) {
                    forgeController.itemHandler.getStackInSlot(OUTPUT_SLOT).grow(recipe.get().getOutput().getCount());
                }
            }
            forgeController.fuelTime = forgeController.fuelTime - recipe.get().fuelPerTick() - 1;
        } else forgeController.smeltProgress = 0;
        if (forgeController.fuelTime > 0) forgeController.fuelTime--;
    }

    protected boolean canSmelt(SmeltingRecipe recipe) {
        ItemStack output = recipe.getResultItem(level.registryAccess());
        output.isEmpty();

        if (this.fuelTime > 0)
            if(this.tier >= recipe.getRequiredTier())
                if (this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + recipe.getOutput().getCount() <= recipe.getOutput().getMaxStackSize())
                    if (recipe.getOutput().getItem() == this.itemHandler.getStackInSlot(OUTPUT_SLOT).getItem() || this.itemHandler.getStackInSlot(OUTPUT_SLOT) == ItemStack.EMPTY)
                        if (recipe.getIngredients().size() > 1) {
                            if (this.itemHandler.getStackInSlot(INPUT_SLOTS[0]).getCount() >= recipe.getIngredients().get(0).getItems()[0].getCount()
                                    && this.itemHandler.getStackInSlot(INPUT_SLOTS[1]).getCount() >= recipe.getIngredients().get(1).getItems()[0].getCount())
                                return true;
                        } else {
                            if (this.itemHandler.getStackInSlot(INPUT_SLOTS[0]).getCount() >= recipe.getIngredients().get(0).getItems()[0].getCount()
                                    || this.itemHandler.getStackInSlot(INPUT_SLOTS[1]).getCount() >= recipe.getIngredients().get(0).getItems()[0].getCount())
                                return true;
                        }
        return false;
    }

    public static int findNumber(ArrayList<Integer> array, int target) {
        int max = Integer.MIN_VALUE;
        for (int num : array) {
            if (num == target) return target;
            if (num > max) max = num;
        }
        return max;
    }

    public Optional<SmeltingRecipe> getMatchingRecipe() {
        if (this.level == null) return Optional.empty();
        ItemStackHandler container = new ItemStackHandler(this.itemHandler.getSlots());
        for (int i = 0; i < INPUT_SLOTS.length; i++) container.setStackInSlot(i, this.itemHandler.getStackInSlot(INPUT_SLOTS[i]));

        ArrayList<SmeltingRecipe> recipes = new ArrayList<>();
        ArrayList<Integer> recipeTiers = new ArrayList<>();
        for (int i = 1; i <= FORGE_TIERS; i++) this.level.getRecipeManager().getRecipeFor(SmeltingRecipe.Type.INSTANCE, new CustomRecipeWrapper(container, i), this.level).ifPresent(recipes::add);
        recipes.stream().forEach(recipe -> recipeTiers.add(recipe.getRequiredTier()));
        int recipeTier = findNumber(recipeTiers, this.tier);

        return this.level.getRecipeManager().getRecipeFor(SmeltingRecipe.Type.INSTANCE, new CustomRecipeWrapper(container, recipeTier), this.level);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean verifyMultiblock() {
        BlockState blockBelowController = level.getBlockState(multiblockPositions.get(0));
        TagKey<Block> ALLOY_SMELTER_BLOCKS = BlockTags.create(new ResourceLocation(AlloySmelter.MOD_ID, "alloy_smelter_blocks_tier" + this.tier));
        if (!blockBelowController.is(ALLOY_SMELTER_BLOCKS) && !blockBelowController.is(Blocks.HOPPER)) return false;
        for (int i = 1; i < multiblockPositions.size(); i++)
            if (!(level.getBlockState(multiblockPositions.get(i)).is(ALLOY_SMELTER_BLOCKS))) return false;
        return true;
    }

    private static List<BlockPos> generateMultiblock(BlockPos controllerPos, Direction facing) {
        final List<BlockPos> posses = new ArrayList<>();
        final BlockPos center = controllerPos.offset(facing.getOpposite().getNormal());
        var offsets = new BlockPos[]{
                center.offset(-1, -1, -1),
                center.offset(0, -1, -1),
                center.offset(1, -1, -1),
                center.offset(-1, -1, 0),
                center.offset(0, -1, 0),
                center.offset(1, -1, 0),
                center.offset(-1, -1, 1),
                center.offset(0, -1, 1),
                center.offset(1, -1, 1)
        };
        for (BlockPos i : offsets) posses.add(i);
        posses.remove(controllerPos.below());
        posses.add(0, controllerPos.below());
        for (int i = 0; i < 2; i++) {
            BlockPos newCenter = center.offset(0, i, 0);
            posses.add(newCenter.east());
            posses.add(newCenter.west());
            posses.add(newCenter.north());
            posses.add(newCenter.south());
        }
        posses.remove(controllerPos);
        return List.copyOf(posses);
    }
}

