package vazkii.quark.content.building.block;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import vazkii.arl.interf.IBlockItemProvider;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.client.render.VariantChestTileEntityRenderer;
import vazkii.quark.content.building.module.VariantChestsModule.IChestTextureProvider;
import vazkii.quark.content.building.tile.VariantChestTileEntity;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

@OnlyIn(value = Dist.CLIENT, _interface = IBlockItemProvider.class)
public class VariantChestBlock extends ChestBlock implements IBlockItemProvider, IQuarkBlock, IChestTextureProvider {

	public final String type;
	private final QuarkModule module;
	private BooleanSupplier enabledSupplier = () -> true;
	
	private String path;

	public VariantChestBlock(String type, QuarkModule module, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, Properties props) {
		super(props, supplier);
		RegistryHelper.registerBlock(this, type + "_chest");
		RegistryHelper.setCreativeTab(this, CreativeModeTab.TAB_DECORATIONS);
		
		this.type = type;
		this.module = module;
		
		path = (this instanceof Compat ? "compat/" : "") + type + "/";
	}
	
	@Override
	public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return false;
	}
	
	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if(isEnabled() || group == CreativeModeTab.TAB_SEARCH)
			super.fillItemCategory(group, items);
	}

	@Override
	public VariantChestBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

	@Nullable
	@Override
	public QuarkModule getModule() {
		return module;
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter worldIn) {
		return new VariantChestTileEntity();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void setISTER(Item.Properties props, Block block) {
		props.setISTER(() -> () -> new BlockEntityWithoutLevelRenderer() {
			private final BlockEntity tile = new VariantChestTileEntity();
			//render
			@Override
			public void renderByItem(ItemStack stack, TransformType transformType, PoseStack matrix, MultiBufferSource buffer, int x, int y) {
				VariantChestTileEntityRenderer.invBlock = block;
				BlockEntityRenderDispatcher.instance.renderItem(tile, matrix, buffer, x, y);
				VariantChestTileEntityRenderer.invBlock = null;
			}
			
		});
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockItem provideItemBlock(Block block, Item.Properties props) {
		setISTER(props, block);
		return new BlockItem(block, props);
	}
	
	public static class Compat extends VariantChestBlock {

		public Compat(String type, String mod, QuarkModule module, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, Properties props) {
			super(type, module, supplier, props);
			setCondition(() -> ModList.get().isLoaded(mod));
		}
		
	}

	@Override
	public String getChestTexturePath() {
		return "model/chest/" + path;
	}

	@Override
	public boolean isTrap() {
		return false;
	}
	
}
