package vazkii.quark.content.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.client.handler.ModKeybindHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;

@LoadModule(category = ModuleCategory.CLIENT, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class AutoWalkKeybindModule extends QuarkModule {

	@OnlyIn(Dist.CLIENT)
	private KeyMapping keybind;

	private boolean autorunning;
	private boolean hadAutoJump;

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		if(enabled)
			keybind = ModKeybindHandler.init("autorun", "caps.lock", ModKeybindHandler.ACCESSIBILITY_GROUP);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onMouseInput(InputEvent.MouseInputEvent event) {
		acceptInput();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		acceptInput();
	}

	private void acceptInput() {
		Minecraft mc = Minecraft.getInstance();

		if(mc.options.keyUp.isDown()) {
			if(autorunning)
				mc.options.autoJump = hadAutoJump;
			
			autorunning = false;
		}
		
		else if(keybind.isDown()) {
			autorunning = !autorunning;

			if(autorunning) {
				hadAutoJump = mc.options.autoJump;
				mc.options.autoJump = true;
			} else mc.options.autoJump = hadAutoJump;
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onInput(InputUpdateEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player != null && autorunning) {
			event.getMovementInput().up = true;
			// [VanillaCopy] magic numbers copied from net.minecraft.util.MovementInputFromOptions
			event.getMovementInput().forwardImpulse = ((LocalPlayer) event.getPlayer()).isMovingSlowly() ? 0.3F : 1F;
		}
	}

}
