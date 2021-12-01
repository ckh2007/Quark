package vazkii.quark.content.tools.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import vazkii.quark.base.handler.RayTraceHandler;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.module.BottledCloudModule;

public class BottledCloudItem extends QuarkItem {

	public BottledCloudItem(QuarkModule module) {
		super("bottled_cloud", module, new Item.Properties().tab(CreativeModeTab.TAB_TOOLS));
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		
		HitResult result = RayTraceHandler.rayTrace(player, world, player, Block.OUTLINE, Fluid.ANY);
		if(result instanceof BlockHitResult) {
			BlockHitResult bresult = (BlockHitResult) result;
			BlockPos pos = bresult.getBlockPos();
			if(!world.isEmptyBlock(pos))
				pos = pos.relative(bresult.getDirection());
			
			if(world.isEmptyBlock(pos)) {
				if(!world.isClientSide)
					world.setBlockAndUpdate(pos, BottledCloudModule.cloud.defaultBlockState());
				
				stack.shrink(1);
				
				if(!player.isCreative()) {
					ItemStack returnStack = new ItemStack(Items.GLASS_BOTTLE);
					if(stack.isEmpty())
						stack = returnStack;
					else if(!player.addItem(returnStack))
						player.drop(returnStack, false);
				}
				
				player.getCooldowns().addCooldown(this, 10);
				return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
			}
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
	}

}
