package vazkii.quark.base.module.config.type;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.client.config.ConfigCategory;
import vazkii.zeta.module.ZetaModule;
import vazkii.quark.base.module.config.ConfigFlagManager;

public interface IConfigType {

	@OnlyIn(Dist.CLIENT) 
	default void setCategory(ConfigCategory category) { }
	
	default void onReload(ZetaModule module, ConfigFlagManager flagManager) { }
	
}
