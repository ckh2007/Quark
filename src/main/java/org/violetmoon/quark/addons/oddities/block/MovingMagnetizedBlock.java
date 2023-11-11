package org.violetmoon.quark.addons.oddities.block;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.violetmoon.quark.addons.oddities.block.be.MagnetizedBlockBlockEntity;
import org.violetmoon.quark.addons.oddities.module.MagnetsModule;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;

/**
 * @author WireSegal
 * Created at 3:05 PM on 2/26/20.
 */
public class MovingMagnetizedBlock extends ZetaBlock implements EntityBlock {
	public static final DirectionProperty FACING = PistonHeadBlock.FACING;

	public MovingMagnetizedBlock(ZetaModule module) {
		super("magnetized_block", module, null, Block.Properties.of(Material.PISTON).strength(-1.0F).dynamicShape().noLootTable().noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Nonnull
	@Override
	public RenderShape getRenderShape(@Nonnull BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public void onRemove(BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			MagnetizedBlockBlockEntity tile = getMagnetTileEntity(worldIn, pos);
			if (tile != null)
				tile.clearMagnetTileEntity();
		}
	}

	@Override
	public boolean useShapeForLightOcclusion(@Nonnull BlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public InteractionResult use(@Nonnull BlockState state, Level worldIn, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand handIn, @Nonnull BlockHitResult hit) {
		if (!worldIn.isClientSide && worldIn.getBlockEntity(pos) == null) {
			worldIn.removeBlock(pos, false);
			return InteractionResult.SUCCESS;
		} else
			return InteractionResult.PASS;
	}

	@Override
	@Nonnull
	public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
		MagnetizedBlockBlockEntity tile = this.getMagnetTileEntity(builder.getLevel(), builder.getParameter(LootContextParams.ORIGIN)); // origin
		return tile == null ? Collections.emptyList() : tile.getMagnetState().getDrops(builder);
	}

	@Override
	@Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	@Nonnull
	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
		MagnetizedBlockBlockEntity tile = this.getMagnetTileEntity(worldIn, pos);
		return tile != null ? tile.getCollisionShape(worldIn, pos) : Shapes.empty();
	}

	@Nullable
	private MagnetizedBlockBlockEntity getMagnetTileEntity(BlockGetter world, Vec3 origin) {
		BlockPos pos = new BlockPos(origin);
		return getMagnetTileEntity(world, pos);
	}

	@Nullable
	private MagnetizedBlockBlockEntity getMagnetTileEntity(BlockGetter world, BlockPos pos) {
		BlockEntity tile = world.getBlockEntity(pos);
		return tile instanceof MagnetizedBlockBlockEntity ? (MagnetizedBlockBlockEntity)tile : null;
	}

	@Override
	@Nonnull
	public ItemStack getCloneItemStack(@Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		return ItemStack.EMPTY;
	}

	@Override
	@Nonnull
	public BlockState rotate(@Nonnull BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	@Nonnull
	public BlockState mirror(@Nonnull BlockState state, Mirror mirrorIn) {
		return rotate(state, mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull PathComputationType type) {
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
		return null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level world, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
		return createTickerHelper(type, MagnetsModule.magnetizedBlockType, MagnetizedBlockBlockEntity::tick);
	}

}
