package vazkii.quark.content.tweaks.module;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.tweaks.client.item.ClockTimeGetter;
import vazkii.quark.content.tweaks.client.item.CompassAngleGetter;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class CompassesWorkEverywhereModule extends QuarkModule {

	@Config public static boolean enableCompassNerf =  true;
	@Config public static boolean enableClockNerf =  true;
	
	@Config public static boolean enableNether =  true;
	@Config public static boolean enableEnd =  true;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		// register = addPropertyOverride
		if(enabled && (enableCompassNerf || enableNether || enableEnd))
			enqueue(() -> ItemProperties.register(Items.COMPASS, new ResourceLocation("angle"), new CompassAngleGetter.Impl()));
		
		if(enabled && enableClockNerf)
			enqueue(() -> ItemProperties.register(Items.CLOCK, new ResourceLocation("time"), new ClockTimeGetter.Impl()));
	}
	
	@SubscribeEvent
	public void onUpdate(PlayerTickEvent event) {
		if(event.phase == Phase.START) {
			for(int i = 0; i < event.player.inventory.getContainerSize(); i++) {
				ItemStack stack = event.player.inventory.getItem(i);
				if(stack.getItem() == Items.COMPASS)
					CompassAngleGetter.tickCompass(event.player, stack);
				else if(stack.getItem() == Items.CLOCK)
					ClockTimeGetter.tickClock(stack);
			}
		}
	}
	
}
