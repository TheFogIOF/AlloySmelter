package sk.alloy_smelter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.recipe.CustomRecipeWrapper;
import sk.alloy_smelter.recipe.SmeltingRecipe;
import sk.alloy_smelter.registry.BlockEntities;
import sk.alloy_smelter.registry.RecipeTypes;
import sk.alloy_smelter.registry.Tags;
import sk.alloy_smelter.screen.ForgeControllerMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = AlloySmelter.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
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

    protected final ContainerData data;

    private final IItemHandler inputFrontHandler;
    private final IItemHandler inputLeftHandler;
    private final IItemHandler inputRightHandler;
    private final IItemHandler outputHandler;

    private int smeltProgress;
    private int maxSmeltProgress;
    private int fuelTime;
    private int burnedFuelTime;

    private final RecipeManager.CachedCheck<CustomRecipeWrapper, SmeltingRecipe> quickCheck;

    public ForgeControllerBlockEntity(BlockPos position, BlockState state) {
        super(BlockEntities.FORGE_CONTROLLER_BLOCK_ENTITY.get(), position, state);
        facing = state.getValue(ForgeControllerBlock.FACING);
        multiblockPositions = generateMultiblock(position, state.getValue(ForgeControllerBlock.FACING));
        tier = ((ForgeControllerBlock) state.getBlock()).tier;

        this.inputFrontHandler = createHopperFrontItemHandler(inventory);
        this.inputLeftHandler = createHopperLeftItemHandler(inventory);
        this.inputRightHandler = createHopperRightItemHandler(inventory);
        this.outputHandler = createHopperBottomItemHandler(inventory);

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> ForgeControllerBlockEntity.this.smeltProgress;
                    case 1 -> ForgeControllerBlockEntity.this.maxSmeltProgress;
                    case 2 -> ForgeControllerBlockEntity.this.fuelTime;
                    case 3 -> ForgeControllerBlockEntity.this.burnedFuelTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> ForgeControllerBlockEntity.this.smeltProgress = value;
                    case 1 -> ForgeControllerBlockEntity.this.maxSmeltProgress = value;
                    case 2 -> ForgeControllerBlockEntity.this.fuelTime = value;
                    case 3 -> ForgeControllerBlockEntity.this.burnedFuelTime = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
        this.quickCheck = RecipeManager.createCheck(RecipeTypes.SMELTING.get());
    }

    public static Direction getSide(Direction direction, int state) {
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

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            BlockEntities.FORGE_CONTROLLER_BLOCK_ENTITY.get(),
            (be, context) -> {
                if (context == getSide(be.facing, 0))
                    return be.inputFrontHandler;
                if (context == getSide(be.facing, 1))
                    return be.inputLeftHandler;
                if (context == getSide(be.facing, 2))
                    return be.inputRightHandler;
                return be.outputHandler;
            }
        );
    }

    @Override
    public void invalidateCapabilities() {
        super.invalidateCapabilities();
    }

    private IItemHandler createHopperFrontItemHandler(ItemStackHandler inventory) {
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
    private IItemHandler createHopperBottomItemHandler(ItemStackHandler inventory) {
        return new IItemHandler() {
            @Override public int getSlots() { return 1; }
            @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(3); }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; } // No insertion
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return inventory.extractItem(3, amount, simulate); }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
        };
    }
    private IItemHandler createHopperLeftItemHandler(ItemStackHandler inventory) {
        return new IItemHandler() {
            @Override public int getSlots() { return 2; }
            @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return inventory.insertItem(1, stack, simulate); }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
        };
    }
    private IItemHandler createHopperRightItemHandler(ItemStackHandler inventory) {
        return new IItemHandler() {
            @Override public int getSlots() { return 2; }
            @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return inventory.insertItem(2, stack, simulate); }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
        };
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        smeltProgress = tag.getInt("smeltProgress");
        fuelTime = tag.getInt("fuelTime");
        burnedFuelTime = tag.getInt("burnedFuelTime");
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("smeltProgress", smeltProgress);
        tag.putInt("fuelTime", fuelTime);
        tag.putInt("burnedFuelTime", burnedFuelTime);
    }

    public void drops() {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, container);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.alloy_smelter.alloy_smelter_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player) {
        return new ForgeControllerMenu(i, playerInventory, this, this.data);
    }

    private CompoundTag writeItems(CompoundTag compound, HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.put("Inventory", inventory.serializeNBT(registries));
        return compound;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return writeItems(new CompoundTag(), registries);
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
        if (forgeController.inventory.getStackInSlot(FUEL_SLOT).is(Tags.ALLOY_SMELTER_FUEL) && forgeController.fuelTime < 1) {
            forgeController.burnedFuelTime = forgeController.inventory.getStackInSlot(FUEL_SLOT).getBurnTime(RecipeType.SMELTING);
            forgeController.fuelTime += forgeController.inventory.getStackInSlot(FUEL_SLOT).getBurnTime(RecipeType.SMELTING);
            forgeController.inventory.getStackInSlot(FUEL_SLOT).shrink(1);
        }

        if (forgeController.fuelTime > 0 && forgeController.verifyMultiblock())
            level.setBlock(blockPos, blockState.setValue(ForgeControllerBlock.LIT, true), 3);
        else level.setBlock(blockPos, blockState.setValue(ForgeControllerBlock.LIT, false), 3);

        if (!forgeController.verifyMultiblock()) return;

        Optional<RecipeHolder<SmeltingRecipe>> recipe = forgeController.getMatchingRecipe();

        if (recipe.isPresent() && forgeController.canSmelt(recipe.get().value())) {
            forgeController.maxSmeltProgress = recipe.get().value().getSmeltingTime();
            forgeController.smeltProgress++;
            if (forgeController.smeltProgress > recipe.get().value().getSmeltingTime()) {
                if (recipe.get().value().getMaterials().size() > 1) {
                    forgeController.inventory.getStackInSlot(INPUT_SLOTS[0]).shrink(recipe.get().value().getMaterials().get(0).count());
                    forgeController.inventory.getStackInSlot(INPUT_SLOTS[1]).shrink(recipe.get().value().getMaterials().get(1).count());
                } else {
                    forgeController.inventory.getStackInSlot(INPUT_SLOTS[0]).shrink(recipe.get().value().getMaterials().get(0).count());
                    forgeController.inventory.getStackInSlot(INPUT_SLOTS[1]).shrink(recipe.get().value().getMaterials().get(0).count());
                }
                forgeController.smeltProgress = 0;
                if (forgeController.inventory.getStackInSlot(OUTPUT_SLOT) == ItemStack.EMPTY) {
                    forgeController.inventory.setStackInSlot(OUTPUT_SLOT, recipe.get().value().getOutput());
                } else if (forgeController.inventory.getStackInSlot(OUTPUT_SLOT).getItem() == recipe.get().value().getOutput().getItem()) {
                    forgeController.inventory.getStackInSlot(OUTPUT_SLOT).grow(recipe.get().value().getOutput().getCount());
                }
            }
            forgeController.fuelTime = forgeController.fuelTime - recipe.get().value().fuelPerTick() - 1;
        } else forgeController.smeltProgress = 0;
        if (forgeController.fuelTime > 0) forgeController.fuelTime--;
    }

    public static int findNumber(ArrayList<Integer> array, int target) {
        int max = Integer.MIN_VALUE;
        for (int num : array) {
            if (num == target) return target;
            if (num > max) max = num;
        }
        return max;
    }

    public Optional<RecipeHolder<SmeltingRecipe>> getMatchingRecipe() {
        if (level == null) return Optional.empty();
        ItemStackHandler container = new ItemStackHandler(this.inventory.getSlots());
        for (int i = 0; i < INPUT_SLOTS.length; i++) container.setStackInSlot(i, this.inventory.getStackInSlot(INPUT_SLOTS[i]));

        ArrayList<RecipeHolder<SmeltingRecipe>> recipes = new ArrayList<>();
        ArrayList<Integer> recipeTiers = new ArrayList<>();
        for (int i = 1; i <= FORGE_TIERS; i++) quickCheck.getRecipeFor(new CustomRecipeWrapper(container, i), this.level).ifPresent(recipes::add);
        recipes.stream().forEach(recipe -> recipeTiers.add(recipe.value().getRequiredTier()));
        int recipeTier = findNumber(recipeTiers, this.tier);

        return quickCheck.getRecipeFor(new CustomRecipeWrapper(container, recipeTier), this.level);
    }

    protected boolean canSmelt(SmeltingRecipe recipe) {
        ItemStack output = recipe.getResultItem(level.registryAccess());
        output.isEmpty();

        if (this.fuelTime > 0)
            if (this.tier >= recipe.getRequiredTier())
                if (this.inventory.getStackInSlot(OUTPUT_SLOT).getCount() + recipe.getOutput().getCount() <= recipe.getOutput().getMaxStackSize())
                    if (recipe.getOutput().getItem() == this.inventory.getStackInSlot(OUTPUT_SLOT).getItem() || this.inventory.getStackInSlot(OUTPUT_SLOT) == ItemStack.EMPTY)
                        if (recipe.getMaterials().size() > 1) {
                            if (recipe.getMaterials().get(0).ingredient().test(this.inventory.getStackInSlot(INPUT_SLOTS[0]))
                                    && recipe.getMaterials().get(1).ingredient().test(this.inventory.getStackInSlot(INPUT_SLOTS[1]))
                                    && this.inventory.getStackInSlot(INPUT_SLOTS[0]).getCount() >= recipe.getMaterials().get(0).count()
                                    && this.inventory.getStackInSlot(INPUT_SLOTS[1]).getCount() >= recipe.getMaterials().get(1).count())
                                return true;
                        } else {
                            if (this.inventory.getStackInSlot(INPUT_SLOTS[0]).getCount() >= recipe.getMaterials().get(0).count()
                                    || this.inventory.getStackInSlot(INPUT_SLOTS[1]).getCount() >= recipe.getMaterials().get(0).count())
                                return true;
                        }
        return false;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean verifyMultiblock() {
        BlockState blockBelowController = level.getBlockState(this.multiblockPositions.get(0));
        TagKey<Block> ALLOY_SMELTER_BLOCKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, "alloy_smelter_blocks_tier" + this.tier));
        if (!blockBelowController.is(ALLOY_SMELTER_BLOCKS) && !blockBelowController.is(Blocks.HOPPER)) return false;
        for (int i = 1; i < this.multiblockPositions.size(); i++)
            if (!(level.getBlockState(this.multiblockPositions.get(i)).is(ALLOY_SMELTER_BLOCKS))) return false;
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

