package vazkii.quark.content.tools.module;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import com.mojang.math.Matrix4f;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.item.AbacusItem;

@LoadModule(category = ModuleCategory.TOOLS, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class AbacusModule extends QuarkModule {

	public static Item abacus;

	@Override
	public void construct() {
		abacus = new AbacusItem(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		enqueue(() -> ItemProperties.register(abacus, new ResourceLocation("count"), AbacusItem::count));
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onHUDRender(RenderGameOverlayEvent event) {
		if(event.getType() == ElementType.ALL) {
			Minecraft mc = Minecraft.getInstance();
			Player player = mc.player;
			if(player != null) {
				ItemStack stack = player.getMainHandItem();
				if(!(stack.getItem() instanceof AbacusItem))
					stack = player.getOffhandItem();

				if(stack.getItem() instanceof AbacusItem) {
					int distance = AbacusItem.getCount(stack, player);
					if(distance > -1) {
						Window window = event.getWindow();
						int x = window.getGuiScaledWidth() / 2 + 10;
						int y = window.getGuiScaledHeight() / 2 - 7;

						mc.getItemRenderer().renderAndDecorateItem(stack, x, y);
						
						String distStr = distance < AbacusItem.MAX_COUNT ? Integer.toString(distance) : (AbacusItem.MAX_COUNT + "+");
						mc.font.drawShadow(event.getMatrixStack(), distStr, x + 17, y + 5, 0xFFFFFF);
					}
				}
			}
		}
	}
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onHighlightBlock(DrawHighlightEvent.HighlightBlock event) {
		VertexConsumer bufferIn = event.getBuffers().getBuffer(RenderType.lines());

		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if(player != null) {
			ItemStack stack = player.getMainHandItem();
			if(!(stack.getItem() instanceof AbacusItem))
				stack = player.getOffhandItem();

			if(stack.getItem() instanceof AbacusItem) {
				int distance = AbacusItem.getCount(stack, player);
				if(distance > -1 && distance <= AbacusItem.MAX_COUNT) {
					BlockPos target = AbacusItem.getBlockPos(stack);

					Camera info = event.getInfo();
					Vec3 view = info.getPosition();

					VoxelShape shape = Shapes.create(new AABB(target));

					HitResult result = mc.hitResult;
					if(result instanceof BlockHitResult) {
						BlockPos source = ((BlockHitResult) result).getBlockPos();
						
						int diffX = source.getX() - target.getX();
						int diffY = source.getY() - target.getY();
						int diffZ = source.getZ() - target.getZ();
						
						if(diffX != 0)
							shape = Shapes.or(shape, Shapes.create(new AABB(target).expandTowards(diffX, 0, 0)));
						if(diffY != 0)
							shape = Shapes.or(shape, Shapes.create(new AABB(target.offset(diffX, 0, 0)).expandTowards(0, diffY, 0)));
						if(diffZ != 0)
							shape = Shapes.or(shape, Shapes.create(new AABB(target.offset(diffX, diffY, 0)).expandTowards(0, 0, diffZ)));
					}

					if(shape != null) {
						List<AABB> list = shape.toAabbs();
						PoseStack matrixStackIn = event.getMatrix();
						
						// everything from here is a vanilla copy pasta but tweaked to have the same colors
						
						double xIn = -view.x;
						double yIn = -view.y;
						double zIn = -view.z;
						
						for(int j = 0; j < list.size(); ++j) {
							float r = 0F;
							float g = 0F;
							float b = 0F;
							float a = 0.4F;
							
							AABB axisalignedbb = list.get(j);

							VoxelShape individual = Shapes.create(axisalignedbb.move(0.0D, 0.0D, 0.0D));
							Matrix4f matrix4f = matrixStackIn.last().pose();
							individual.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
								bufferIn.vertex(matrix4f, (float)(minX + xIn), (float)(minY + yIn), (float)(minZ + zIn)).color(r, g, b, a).endVertex();
								bufferIn.vertex(matrix4f, (float)(maxX + xIn), (float)(maxY + yIn), (float)(maxZ + zIn)).color(r, g, b, a).endVertex();
							});
						}
						
						event.setCanceled(true);
					}
				}
			}
		}
	}

}
