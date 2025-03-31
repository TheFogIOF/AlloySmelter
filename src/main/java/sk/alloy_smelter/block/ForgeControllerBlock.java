package sk.alloy_smelter.block;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import sk.alloy_smelter.AlloySmelter;
import sk.alloy_smelter.registry.BlockEntities;

import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeControllerBlock extends BaseEntityBlock implements EntityBlock
{
    public static final DeferredRegister<MapCodec<? extends Block>> REGISTRAR = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, AlloySmelter.MOD_ID);
    public static final Supplier<MapCodec<ForgeControllerBlock>> CODEC = REGISTRAR.register(
            "simple",
            () -> RecordCodecBuilder.mapCodec(inst ->
                    inst.group(
                            BlockBehaviour.propertiesCodec(),
                            Codec.INT.fieldOf("tier").forGetter(ForgeControllerBlock::getTier)
                    ).apply(inst, ForgeControllerBlock::new)
            )
    );
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public final int tier;

    public int getTier()
    {
        return this.tier;
    }

    public ForgeControllerBlock(Properties properties, int tier) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
        this.tier = tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LIT, false);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof ForgeControllerBlockEntity) {
                ((ForgeControllerBlockEntity) blockEntity).drops();
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (level.getBlockEntity(pos) instanceof ForgeControllerBlockEntity controller) {
            if (!controller.verifyMultiblock()) {
                if (level.isClientSide) player.displayClientMessage(Component.translatable("message.alloy_smelter.invalid_multiblock").withColor(8421504), true);
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide) {
                player.openMenu(controller, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntities.FORGE_CONTROLLER_BLOCK_ENTITY.get().create(blockPos, blockState);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return level.isClientSide ?
                createTickerHelper(blockEntity, BlockEntities.FORGE_CONTROLLER_BLOCK_ENTITY.get(), ForgeControllerBlockEntity::clientTick) :
                createServerTicker(level, blockEntity, BlockEntities.FORGE_CONTROLLER_BLOCK_ENTITY.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createServerTicker(Level level, BlockEntityType<T> serverType, BlockEntityType<? extends ForgeControllerBlockEntity> clientType) {
        return level instanceof ServerLevel serverlevel ? createTickerHelper(
                serverType,
                clientType,
                (a, b, c, d) -> ForgeControllerBlockEntity.serverTick(serverlevel, b, c, d)
            ) : null;
    }
}
