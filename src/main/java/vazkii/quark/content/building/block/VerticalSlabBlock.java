package vazkii.quark.content.building.block;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.arl.interf.IBlockColorProvider;
import vazkii.arl.interf.IItemColorProvider;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.block.QuarkSlabBlock;
import vazkii.quark.base.datagen.QuarkBlockStateProvider;
import vazkii.quark.base.datagen.QuarkLootTableProvider;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.world.block.IMyaliteColorProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class VerticalSlabBlock extends QuarkBlock implements SimpleWaterloggedBlock, IBlockColorProvider {

	public static final EnumProperty<VerticalSlabType> TYPE = EnumProperty.create("type", VerticalSlabType.class);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public final Block parent;

	public VerticalSlabBlock(Block parent, QuarkModule module) {
		super(parent.getRegistryName().getPath().replace("_slab", "_vertical_slab"), module, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(parent));
		this.parent = parent;

		if(!(parent instanceof SlabBlock))
			throw new IllegalArgumentException("Can't rotate a non-slab block into a vertical slab.");

		if(parent instanceof QuarkSlabBlock quarkSlab)
			setCondition(quarkSlab.parent::isEnabled);

		registerDefaultState(defaultBlockState().setValue(TYPE, VerticalSlabType.NORTH).setValue(WATERLOGGED, false));
	}

	@Nonnull
	@Override
	public BlockState rotate(BlockState state, @Nonnull Rotation rot) {
		return state.getValue(TYPE) == VerticalSlabType.DOUBLE ? state : state.setValue(TYPE, VerticalSlabType.fromDirection(rot.rotate(state.getValue(TYPE).direction)));
	}

	@Nonnull
	@Override
	public BlockState mirror(BlockState state, @Nonnull Mirror mirrorIn) {
		VerticalSlabType type = state.getValue(TYPE);
		if(type == VerticalSlabType.DOUBLE || mirrorIn == Mirror.NONE)
			return state;

		if((mirrorIn == Mirror.LEFT_RIGHT && type.direction.getAxis() == Axis.Z)
				|| (mirrorIn == Mirror.FRONT_BACK && type.direction.getAxis() == Axis.X))
			return state.setValue(TYPE, VerticalSlabType.fromDirection(state.getValue(TYPE).direction.getOpposite()));

		return state;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return state.getValue(TYPE) != VerticalSlabType.DOUBLE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TYPE, WATERLOGGED);
	}

	@Nonnull
	@Override
	public VoxelShape getShape(BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
		return state.getValue(TYPE).shape;
	}

	@Override
	public boolean isConduitFrame(BlockState state, LevelReader world, BlockPos pos, BlockPos conduit) {
		return parent.isConduitFrame(state, world, pos, conduit);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos blockpos = context.getClickedPos();
		BlockState blockstate = context.getLevel().getBlockState(blockpos);
		if(blockstate.getBlock() == this)
			return blockstate.setValue(TYPE, VerticalSlabType.DOUBLE).setValue(WATERLOGGED, false);

		FluidState fluid = context.getLevel().getFluidState(blockpos);
		BlockState retState = defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
		Direction direction = getDirectionForPlacement(context);
		VerticalSlabType type = VerticalSlabType.fromDirection(direction);

		return retState.setValue(TYPE, type);
	}

	private Direction getDirectionForPlacement(BlockPlaceContext context) {
		Direction direction = context.getClickedFace();
		if(direction.getAxis() != Axis.Y)
			return direction;

		BlockPos pos = context.getClickedPos();
		Vec3 vec = context.getClickLocation().subtract(new Vec3(pos.getX(), pos.getY(), pos.getZ())).subtract(0.5, 0, 0.5);
		double angle = Math.atan2(vec.x, vec.z) * -180.0 / Math.PI;
		return Direction.fromYRot(angle).getOpposite();
	}

	@Override
	public boolean canBeReplaced(BlockState state, @Nonnull BlockPlaceContext useContext) {
		ItemStack itemstack = useContext.getItemInHand();
		VerticalSlabType slabtype = state.getValue(TYPE);
		return slabtype != VerticalSlabType.DOUBLE && itemstack.getItem() == this.asItem() &&
			(useContext.replacingClickedOnBlock() && (useContext.getClickedFace() == slabtype.direction && getDirectionForPlacement(useContext) == slabtype.direction)
					|| (!useContext.replacingClickedOnBlock() && useContext.getClickedFace().getAxis() != slabtype.direction.getAxis()));
	}

	@Nonnull
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public boolean placeLiquid(@Nonnull LevelAccessor worldIn, @Nonnull BlockPos pos, BlockState state, @Nonnull FluidState fluidStateIn) {
		return state.getValue(TYPE) != VerticalSlabType.DOUBLE && SimpleWaterloggedBlock.super.placeLiquid(worldIn, pos, state, fluidStateIn);
	}

	@Override
	public boolean canPlaceLiquid(@Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, BlockState state, @Nonnull Fluid fluidIn) {
		return state.getValue(TYPE) != VerticalSlabType.DOUBLE && SimpleWaterloggedBlock.super.canPlaceLiquid(worldIn, pos, state, fluidIn);
	}

	@Nonnull
	@Override
	public BlockState updateShape(@Nonnull BlockState stateIn, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor worldIn, @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
		if(stateIn.getValue(WATERLOGGED))
			worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));

		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull PathComputationType type) {
		return type == PathComputationType.WATER && worldIn.getFluidState(pos).is(FluidTags.WATER);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockColor getBlockColor() {
		return parent instanceof IBlockColorProvider provider ? provider.getBlockColor() : null;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ItemColor getItemColor() {
		return parent instanceof IItemColorProvider provider ? provider.getItemColor() : null;
	}

	@Nullable
	@Override
	public TagKey<Block> mineWith() {
		return parent instanceof IQuarkBlock quarkBlock ? quarkBlock.mineWith() : super.mineWith();
	}

	@Override
	public void dataGen(QuarkBlockStateProvider states) {
		Block trueParent = parent;
		if (trueParent instanceof QuarkSlabBlock slab)
			trueParent = slab.parent.getBlock();

		ResourceLocation parentName = trueParent.getRegistryName();
		ResourceLocation possibleTarget = new ResourceLocation(parentName.getNamespace(), parentName.getPath().replace("_slab", "").replace("waxed_", ""));
		Block block = ForgeRegistries.BLOCKS.getValue(possibleTarget);
		if (block != null && block != Blocks.AIR) {
			ResourceLocation texture = states.blockTexture(block);
			// Get around some vanilla naming
			if (texture.getNamespace().equals("minecraft") && texture.getPath().contains("smooth_") && !texture.getPath().contains("smooth_stone")) {
				texture = new ResourceLocation(texture.getNamespace(), texture.getPath().replace("smooth_", "") + "_top");
				if (texture.getPath().contains("quartz"))
					texture = new ResourceLocation(texture.getNamespace(), texture.getPath().replace("quartz", "quartz_block"));
			}
			if (trueParent instanceof IMyaliteColorProvider)
				states.tintedVerticalSlabBlock(this, states.baseBlockTexture(block), texture);
			else
				states.verticalSlabBlock(this, states.baseBlockTexture(block), texture);
		}
		// If we can't find that block, just do it manually
		states.simpleBlockItem(this);
	}

	@Override
	public void dataGen(QuarkLootTableProvider tableProvider, Map<Block, LootTable.Builder> lootTables) {
		lootTables.put(this, LootTable.lootTable()
				.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(this)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))
										.when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(this)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(VerticalSlabBlock.TYPE, VerticalSlabType.DOUBLE))))
								.apply(ApplyExplosionDecay.explosionDecay()))));
	}

	public enum VerticalSlabType implements StringRepresentable {
		NORTH(Direction.NORTH),
		SOUTH(Direction.SOUTH),
		WEST(Direction.WEST),
		EAST(Direction.EAST),
		DOUBLE(null);

		private final String name;
		public final Direction direction;
		public final VoxelShape shape;

		VerticalSlabType(Direction direction) {
			this.name = direction == null ? "double" : direction.getSerializedName();
			this.direction = direction;

			if(direction == null)
				shape = Shapes.block();
			else {
				double min = 0;
				double max = 8;
				if(direction.getAxisDirection() == AxisDirection.NEGATIVE) {
					min = 8;
					max = 16;
				}

				if(direction.getAxis() == Axis.X)
					shape = Block.box(min, 0, 0, max, 16, 16);
				else shape = Block.box(0, 0, min, 16, 16, max);
			}
		}

		@Override
		public String toString() {
			return name;
		}

		@Nonnull
		@Override
		public String getSerializedName() {
			return name;
		}

		public static VerticalSlabType fromDirection(Direction direction) {
			for(VerticalSlabType type : VerticalSlabType.values())
				if(type.direction != null && direction == type.direction)
					return type;

			return null;
		}

	}

}
