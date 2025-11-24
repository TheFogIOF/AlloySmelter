package sk.alloy_smelter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.Config;
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
    private final ItemStackHandler inventory = new ItemStackHandler(7) {
        @Override
        protected void onContentsChanged(int slot) {
            inventoryChanged();
        }
    };

    private static final int FUEL_SLOT = 0;
    private static final int[] INPUT_SLOTS = {1, 2, 3, 4, 5};
    private static final int OUTPUT_SLOT = 6;

    private static final int FORGE_TIERS = 3;

    private final int tier;
    private final List<BlockPos> multiblockPositions;
    private final Direction facing;

    protected final ContainerData data;
    /*
    private final IItemHandler inputFrontHandler;
    private final IItemHandler inputLeftHandler;
    private final IItemHandler inputRightHandler;
    */
    private final IItemHandler inputHandler;
    private final IItemHandler outputHandler;
    private final IItemHandler fullHandler;

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

        this.inputHandler = insertItemCapability(inventory);
        this.outputHandler = extractItemCapability(inventory);
        this.fullHandler = fullItemCapability();

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

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            BlockEntities.FORGE_CONTROLLER_BLOCK_ENTITY.get(),
            (be, context) -> {
                if (context == Direction.DOWN) return be.outputHandler;
                if (context == Direction.UP) return be.inputHandler;
                return be.fullHandler;
            }
        );
    }

    @Override
    public void invalidateCapabilities() {
        super.invalidateCapabilities();
    }

    private IItemHandler insertItemCapability(ItemStackHandler inventory) {
        return new IItemHandler() {
            @Override public int getSlots() { return inventory.getSlots(); }
            @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    if (i != FUEL_SLOT) {
                        ItemStack remaining = inventory.insertItem(i, stack, simulate);
                        if (remaining.getCount() != stack.getCount()) {
                            return remaining;
                        }
                    }
                }
                return stack;
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
        };
    }
    private IItemHandler extractItemCapability(ItemStackHandler inventory) {
        return new IItemHandler() {
            @Override public int getSlots() { return 1; }
            @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(OUTPUT_SLOT); }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return inventory.extractItem(OUTPUT_SLOT, amount, simulate); }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
        };
    }
    public IItemHandler fullItemCapability() {
        return new IItemHandler() {
            @Override public int getSlots() { return inventory.getSlots(); }
            @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return inventory.insertItem(slot, stack, simulate);
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return inventory.extractItem(slot, amount, simulate);
            }
            @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) {
                return inventory.isItemValid(slot, stack);
            }
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
            forgeController.burnedFuelTime = forgeController.inventory.getStackInSlot(FUEL_SLOT).getBurnTime(RecipeType.SMELTING);
            forgeController.fuelTime += forgeController.inventory.getStackInSlot(FUEL_SLOT).getBurnTime(RecipeType.SMELTING);
            forgeController.inventory.getStackInSlot(FUEL_SLOT).shrink(1);
        }

        BlockPos center = blockPos.offset(forgeController.facing.getOpposite().getNormal());
        if (forgeController.fuelTime > 0 && forgeController.verifyMultiblock()) {
            level.setBlock(blockPos, blockState.setValue(ForgeControllerBlock.LIT, true), 3);
        }
        else {
            level.setBlock(blockPos, blockState.setValue(ForgeControllerBlock.LIT, false), 3);
        }

        if (!forgeController.verifyMultiblock()) return;

        if (forgeController.fuelTime > 0 && Config.ENABLE_PASSIVE_FUEL_CONSUMPTION.get()) forgeController.fuelTime--;

        Optional<RecipeHolder<SmeltingRecipe>> recipeHolder = forgeController.getMatchingRecipe();

        if (recipeHolder.isPresent() && forgeController.canSmelt(recipeHolder.get().value())) {
            SmeltingRecipe recipe = recipeHolder.get().value();
            forgeController.maxSmeltProgress = recipe.getSmeltingTime();
            forgeController.smeltProgress++;
            if (forgeController.smeltProgress > recipe.getSmeltingTime()) {
                forgeController.smeltProgress = 0;

                List<SmeltingRecipe.Material> materials = recipe.getMaterials();
                if (materials.size() > 1) {
                    for (int i = 0; i < materials.size(); i++) {
                        forgeController.inventory.getStackInSlot(INPUT_SLOTS[i]).shrink(materials.get(i).count());
                    }
                } else {
                    for (int slot : INPUT_SLOTS) {
                        forgeController.inventory.getStackInSlot(slot).shrink(materials.get(0).count());
                    }
                }

                if (forgeController.inventory.getStackInSlot(OUTPUT_SLOT) == ItemStack.EMPTY) forgeController.inventory.setStackInSlot(OUTPUT_SLOT, recipe.getOutput());
                else if (forgeController.inventory.getStackInSlot(OUTPUT_SLOT).getItem() == recipe.getOutput().getItem()) forgeController.inventory.getStackInSlot(OUTPUT_SLOT).grow(recipe.getOutput().getCount());
            }
            forgeController.fuelTime = forgeController.fuelTime - recipe.fuelPerTick() + (Config.ENABLE_PASSIVE_FUEL_CONSUMPTION.get() ? 1 : 0);
        }
        else forgeController.smeltProgress = 0;
    }

    public Optional<RecipeHolder<SmeltingRecipe>> getMatchingRecipe() {
        if (level == null) return Optional.empty();

        ItemStackHandler container = new ItemStackHandler(this.inventory.getSlots());
        for (int i = 0; i < INPUT_SLOTS.length; i++) container.setStackInSlot(i, this.inventory.getStackInSlot(INPUT_SLOTS[i]));

        for (int recipeTier = FORGE_TIERS; recipeTier >= 1; recipeTier--)
        {
            if (recipeTier <= this.tier) {
                Optional<RecipeHolder<SmeltingRecipe>> recipe = quickCheck.getRecipeFor(new CustomRecipeWrapper(container, recipeTier), this.level);
                if (recipe.isPresent()) return recipe;
            }
        }
        return Optional.empty();
    }

    protected boolean canSmelt(SmeltingRecipe recipe) {
        ItemStack output = recipe.getResultItem(level.registryAccess());
        output.isEmpty();

        if (!(this.fuelTime > 0)) return false;
        if (!(this.tier >= recipe.getRequiredTier())) return false;
        if (!(this.inventory.getStackInSlot(OUTPUT_SLOT).getCount() + recipe.getOutput().getCount() <= recipe.getOutput().getMaxStackSize())) return false;
        if (!(recipe.getOutput().getItem() == this.inventory.getStackInSlot(OUTPUT_SLOT).getItem() || this.inventory.getStackInSlot(OUTPUT_SLOT) == ItemStack.EMPTY)) return false;
        List<SmeltingRecipe.Material> materials = recipe.getMaterials();

        if (materials.size() > 1) {
            for (int i = 0; i < materials.size(); i++) {
                if (!materials.get(i).ingredient().test(this.inventory.getStackInSlot(INPUT_SLOTS[i]))
                        && this.inventory.getStackInSlot(INPUT_SLOTS[i]).getCount() < materials.get(i).count()
                ) return false;
            }
            return true;
        } else {
            for (int slot : INPUT_SLOTS) {
                if (this.inventory.getStackInSlot(slot).getCount() >= materials.getFirst().count()) return true;
            }
        }
        return false;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean verifyMultiblock() {
        BlockState blockBelowController = level.getBlockState(this.multiblockPositions.get(0));
        BlockState blockAboveController = level.getBlockState(this.multiblockPositions.get(1));
        TagKey<Block> ALLOY_SMELTER_BLOCKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, "alloy_smelter_blocks_tier" + this.tier));
        TagKey<Block> ALLOY_SMELTER_HOPPER = BlockTags.create(ResourceLocation.fromNamespaceAndPath(AlloySmelter.MOD_ID, "alloy_smelter_hopper"));
        if (!blockBelowController.is(ALLOY_SMELTER_BLOCKS) && !blockBelowController.is(ALLOY_SMELTER_HOPPER)) return false;
        if (!blockAboveController.is(ALLOY_SMELTER_BLOCKS) && !blockAboveController.is(ALLOY_SMELTER_HOPPER)) return false;
        for (int i = 2; i < this.multiblockPositions.size(); i++)
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
        for (int i = 0; i < 2; i++) {
            BlockPos newCenter = center.offset(0, i, 0);
            posses.add(newCenter.east());
            posses.add(newCenter.west());
            posses.add(newCenter.north());
            posses.add(newCenter.south());
        }
        posses.remove(controllerPos.above());
        posses.remove(controllerPos.below());
        posses.remove(controllerPos);
        posses.add(0, controllerPos.below());
        posses.add(1, controllerPos.above());
        return List.copyOf(posses);
    }
}

