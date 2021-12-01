package vazkii.quark.content.building.block;

import java.util.function.BooleanSupplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import vazkii.arl.interf.IBlockColorProvider;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.module.HedgesModule;
import vazkii.quark.content.world.block.BlossomLeavesBlock;

public class HedgeBlock extends FenceBlock implements IQuarkBlock, IBlockColorProvider {

	private final QuarkModule module;
	final Block leaf;
	private BooleanSupplier enabledSupplier = () -> true;

	public static final BooleanProperty EXTEND = BooleanProperty.create("extend");

	public HedgeBlock(QuarkModule module, Block fence, Block leaf) {
		super(Block.Properties.copy(fence));

		this.module = module;
		this.leaf = leaf;

		if (leaf instanceof BlossomLeavesBlock) {
			String colorName = leaf.getRegistryName().getPath().replaceAll("_blossom_leaves", "");
  		RegistryHelper.registerBlock(this, colorName + "_blossom_hedge");
		} else {
			RegistryHelper.registerBlock(this, fence.getRegistryName().getPath().replaceAll("_fence", "_hedge"));
		}
		
		RegistryHelper.setCreativeTab(this, CreativeModeTab.TAB_DECORATIONS);

		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);

		registerDefaultState(defaultBlockState().setValue(EXTEND, false));
	}
	
	@Override
	public boolean connectsTo(BlockState state, boolean isSideSolid, Direction direction) {
		return state.getBlock().is(HedgesModule.hedgesTag);
	}
	
	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
		return facing == Direction.UP && !state.getValue(WATERLOGGED) && plantable.getPlantType(world, pos) == PlantType.PLAINS;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockGetter iblockreader = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		BlockPos down = blockpos.below();
		BlockState downState = iblockreader.getBlockState(down);

		return super.getStateForPlacement(context)
				.setValue(EXTEND, downState.getBlock() instanceof HedgeBlock);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.getValue(WATERLOGGED)) {
			worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
		}

		if(facing == Direction.DOWN)
			return stateIn.setValue(EXTEND, facingState.getBlock() instanceof HedgeBlock);
		
		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(EXTEND);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockColor getBlockColor() {
		final BlockColors colors = Minecraft.getInstance().getBlockColors();
		final BlockState leafState = leaf.defaultBlockState();
		return (state, world, pos, tintIndex) -> colors.getColor(leafState, world, pos, tintIndex);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ItemColor getItemColor() {
		final ItemColors colors = Minecraft.getInstance().getItemColors();
		final ItemStack leafStack = new ItemStack(leaf);
		return (stack, tintIndex) -> colors.getColor(leafStack, tintIndex);
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if(isEnabled() || group == CreativeModeTab.TAB_SEARCH)
			super.fillItemCategory(group, items);
	}

	@Override
	public QuarkModule getModule() {
		return module;
	}

	@Override
	public HedgeBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

}
