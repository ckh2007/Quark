package org.violetmoon.quark.content.management.client.screen.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.zeta.util.BooleanSuppliers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class MiniInventoryButton extends Button {

	private final Supplier<List<Component>> tooltip;

	private final int type;
	private final AbstractContainerScreen<?> parent;
	private final int startX;

	private BooleanSupplier shiftTexture = BooleanSuppliers.FALSE;

	@Deprecated(forRemoval = true) //just bincompat for the Consumer-style API instead of the Supplier, in case anyones adding chest buttons (??)
	public MiniInventoryButton(AbstractContainerScreen<?> parent, int type, int x, int y, Consumer<List<String>> legacyTooltip, OnPress onPress) {
		this(parent, type, x, y, () -> {
			List<String> toConsume = new ArrayList<>();
			legacyTooltip.accept(toConsume);
			return toConsume.stream().map(z -> (Component) Component.translatable(z)).toList();
		}, onPress);
	}

	public MiniInventoryButton(AbstractContainerScreen<?> parent, int type, int x, int y, Supplier<List<Component>> tooltip, OnPress onPress) {
		super(new Button.Builder(Component.literal(""), onPress).size(10, 10).pos(parent.getGuiLeft() + x, parent.getGuiTop() + y));
		this.parent = parent;
		this.type = type;
		this.tooltip = tooltip;
		this.startX = x;
	}

	public MiniInventoryButton(AbstractContainerScreen<?> parent, int type, int x, int y, Component tooltip, OnPress onPress) {
		this(parent, type, x, y, () -> List.of(tooltip), onPress);
	}

	public MiniInventoryButton(AbstractContainerScreen<?> parent, int type, int x, int y, String tooltipKey, OnPress onPress) {
		this(parent, type, x, y, Component.translatable(tooltipKey), onPress);
	}

	public MiniInventoryButton setTextureShift(BooleanSupplier func) {
		shiftTexture = func;
		return this;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if(parent instanceof RecipeUpdateListener)
			setX(parent.getGuiLeft() + startX);

		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		int u = type * width;
		int v = 25 + (isHovered ? height : 0);
		if(shiftTexture.getAsBoolean())
			v += (height * 2);

		guiGraphics.blit(ClientUtil.GENERAL_ICONS, getX(), getY(), u, v, width, height);

		//we could use vanilla setTooltip, except the tooltip can change (hence the supplier)
		if(isHovered)
			guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip.get(), mouseX, mouseY);
	}

	@NotNull
	@Override
	protected MutableComponent createNarrationMessage() {
		List<Component> resolvedTooltip = this.tooltip.get();
		return resolvedTooltip.isEmpty() ? Component.literal("") : Component.translatable("gui.narrate.button", resolvedTooltip.get(0));
	}

}
