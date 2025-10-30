package sk.alloy_smelter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import sk.alloy_smelter.Config;
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
    private final ItemStackHandler inventory = new ItemStackHandler(4) {
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
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return inventory.insertItem(0, stack, simulate); }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
        };
    }
    private IItemHandler createHopperBottomItemHandler() {
        return new IItemHandler() {
            @Override public int getSlots() { return 1; }
            @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(3); }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; } // No insertion
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return inventory.extractItem(3, amount, simulate); }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
        };
    }
    private IItemHandler createHopperLeftItemHandler() {
        return new IItemHandler() {
            @Override public int getSlots() { return 2; }
            @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return inventory.insertItem(1, stack, simulate); }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
        };
    }
    private IItemHandler createHopperRightItemHandler() {
        return new IItemHandler() {
            @Override public int getSlots() { return 2; }
            @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return inventory.insertItem(2, stack, simulate); }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
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
        this.playerItemHandler = LazyOptional.of(() -> this.inventory);
        this.hopperItemHandler[0] = LazyOptional.of(() -> createHopperFrontItemHandler());
        this.hopperItemHandler[1] = LazyOptional.of(() -> createHopperBottomItemHandler());
        this.hopperItemHandler[2] = LazyOptional.of(() -> createHopperLeftItemHandler());
        this.hopperItemHandler[3] = LazyOptional.of(() -> createHopperRightItemHandler());
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(this.inventory.getSlots());
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            inventory.setItem(i, this.inventory.getStackInSlot(i));
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
        compoundTag.put("inventory", inventory.serializeNBT());
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
        inventory.deserializeNBT(compoundTag.getCompound("inventory"));
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, ForgeControllerBlockEntity forgeController) {
        if (!forgeController.verifyMultiblock()) return;
        if (!blockState.getValue(ForgeControllerBlock.LIT)) return;
        if (level.random.nextInt(10) == 0) level.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.3F + level.random.nextFloat(), level.random.nextFloat() * 0.7F + 0.5F, false);
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
        if (forgeController.inventory.getStackInSlot(FUEL_SLOT).is(Tags.ALLOY_SMELTER_FUEL) && forgeController.fuelTime < 1) {
            forgeController.burnedFuelTime = ForgeHooks.getBurnTime(forgeController.inventory.getStackInSlot(FUEL_SLOT), RecipeType.SMELTING);
            forgeController.fuelTime += ForgeHooks.getBurnTime(forgeController.inventory.getStackInSlot(FUEL_SLOT), RecipeType.SMELTING);
            forgeController.inventory.getStackInSlot(FUEL_SLOT).shrink(1);
        }

        if (forgeController.fuelTime > 0 && forgeController.verifyMultiblock())
            level.setBlock(blockPos, blockState.setValue(ForgeControllerBlock.LIT, true), 3);
        else level.setBlock(blockPos, blockState.setValue(ForgeControllerBlock.LIT, false), 3);

        if (!forgeController.verifyMultiblock()) return;

        if (forgeController.fuelTime > 0 && Config.ENABLE_PASSIVE_FUEL_CONSUMPTION.get()) forgeController.fuelTime--;

        Optional<SmeltingRecipe> recipe = forgeController.getMatchingRecipe();

        if (recipe.isPresent() && forgeController.canSmelt(recipe.get())) {
            forgeController.smeltProgress++;
            if (forgeController.smeltProgress > recipe.get().getSmeltingTime()) {
                forgeController.smeltProgress = 0;

                if (recipe.get().getIngredients().size() > 1) {
                    forgeController.inventory.getStackInSlot(INPUT_SLOTS[0]).shrink(recipe.get().getIngredients().get(0).getItems()[0].getCount());
                    forgeController.inventory.getStackInSlot(INPUT_SLOTS[1]).shrink(recipe.get().getIngredients().get(1).getItems()[0].getCount());
                } else {
                    forgeController.inventory.getStackInSlot(INPUT_SLOTS[0]).shrink(recipe.get().getIngredients().get(0).getItems()[0].getCount());
                    forgeController.inventory.getStackInSlot(INPUT_SLOTS[1]).shrink(recipe.get().getIngredients().get(0).getItems()[0].getCount());
                }

                if (forgeController.inventory.getStackInSlot(OUTPUT_SLOT) == ItemStack.EMPTY) {
                    forgeController.inventory.setStackInSlot(OUTPUT_SLOT, recipe.get().getOutput());
                } else if (forgeController.inventory.getStackInSlot(OUTPUT_SLOT).getItem() == recipe.get().getOutput().getItem()) {
                    forgeController.inventory.getStackInSlot(OUTPUT_SLOT).grow(recipe.get().getOutput().getCount());
                }
            }
            forgeController.fuelTime = forgeController.fuelTime - recipe.get().fuelPerTick() + (Config.ENABLE_PASSIVE_FUEL_CONSUMPTION.get() ? 1 : 0);
        } else forgeController.smeltProgress = 0;
    }

    protected boolean canSmelt(SmeltingRecipe recipe) {
        ItemStack output = recipe.getResultItem(level.registryAccess());
        output.isEmpty();

        if (this.fuelTime > 0)
            if(this.tier >= recipe.getRequiredTier())
                if (this.inventory.getStackInSlot(OUTPUT_SLOT).getCount() + recipe.getOutput().getCount() <= recipe.getOutput().getMaxStackSize())
                    if (recipe.getOutput().getItem() == this.inventory.getStackInSlot(OUTPUT_SLOT).getItem() || this.inventory.getStackInSlot(OUTPUT_SLOT) == ItemStack.EMPTY)
                        if (recipe.getIngredients().size() > 1) {
                            if (this.inventory.getStackInSlot(INPUT_SLOTS[0]).getCount() >= recipe.getIngredients().get(0).getItems()[0].getCount()
                                    && this.inventory.getStackInSlot(INPUT_SLOTS[1]).getCount() >= recipe.getIngredients().get(1).getItems()[0].getCount())
                                return true;
                        } else {
                            if (this.inventory.getStackInSlot(INPUT_SLOTS[0]).getCount() >= recipe.getIngredients().get(0).getItems()[0].getCount()
                                    || this.inventory.getStackInSlot(INPUT_SLOTS[1]).getCount() >= recipe.getIngredients().get(0).getItems()[0].getCount())
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

        ItemStackHandler container = new ItemStackHandler(this.inventory.getSlots());
        for (int i = 0; i < INPUT_SLOTS.length; i++) container.setStackInSlot(i, this.inventory.getStackInSlot(INPUT_SLOTS[i]));

        for (int recipeTier = FORGE_TIERS; recipeTier >= 1; recipeTier--)
        {
            if (recipeTier <= this.tier) {
                Optional<SmeltingRecipe> recipe = level.getRecipeManager().getRecipeFor(SmeltingRecipe.Type.INSTANCE, new CustomRecipeWrapper(container, recipeTier), this.level);
                if (recipe.isPresent()) return recipe;
            }
        }
        return Optional.empty();
    }

    public ItemStackHandler getItemHandler() {
        return this.inventory;
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

