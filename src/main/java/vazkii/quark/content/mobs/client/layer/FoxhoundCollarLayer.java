/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 13:31 AM (EST)]
 */
package vazkii.quark.content.mobs.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import vazkii.quark.base.Quark;
import vazkii.quark.content.mobs.client.model.FoxhoundModel;
import vazkii.quark.content.mobs.entity.FoxhoundEntity;

public class FoxhoundCollarLayer extends RenderLayer<FoxhoundEntity, FoxhoundModel> {

	private static final ResourceLocation WOLF_COLLAR = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/foxhound/collar.png");

	public FoxhoundCollarLayer(RenderLayerParent<FoxhoundEntity, FoxhoundModel> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack matrix, MultiBufferSource buffer, int light, FoxhoundEntity foxhound,  float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
		if (foxhound.isTame() && !foxhound.isInvisible()) {
			float[] afloat = foxhound.getCollarColor().getTextureDiffuseColors();
			renderColoredCutoutModel(getParentModel(), WOLF_COLLAR, matrix, buffer, light, foxhound, afloat[0], afloat[1], afloat[2]);
		}
	}

}
