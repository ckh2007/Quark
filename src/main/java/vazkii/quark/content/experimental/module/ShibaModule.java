package vazkii.quark.content.experimental.module;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.handler.EntityAttributeHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.config.type.CompoundBiomeConfig;
import vazkii.quark.base.module.config.type.EntitySpawnConfig;
import vazkii.quark.base.world.EntitySpawnHandler;
import vazkii.quark.content.experimental.shiba.client.render.ShibaRenderer;
import vazkii.quark.content.experimental.shiba.entity.ShibaEntity;

@LoadModule(category = ModuleCategory.EXPERIMENTAL, enabledByDefault = false)
public class ShibaModule extends QuarkModule {

	public static EntityType<ShibaEntity> shibaType;
	
	@Config
	public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(40, 1, 3, CompoundBiomeConfig.fromBiomeTypes(false, BiomeDictionary.Type.MOUNTAIN));
	
	@Override
	public void construct() {
		shibaType = EntityType.Builder.of(ShibaEntity::new, MobCategory.CREATURE)
				.sized(0.8F, 0.8F)
				.clientTrackingRange(8)
				.setCustomClientFactory((spawnEntity, world) -> new ShibaEntity(shibaType, world))
				.build("shiba");
		RegistryHelper.register(shibaType, "shiba");
		
		EntitySpawnHandler.registerSpawn(this, shibaType, MobCategory.CREATURE, Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, spawnConfig);
		EntitySpawnHandler.addEgg(shibaType, 0xa86741, 0xe8d5b6, spawnConfig);
		
		EntityAttributeHandler.put(shibaType, Wolf::createAttributes);
	}
	

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(shibaType, ShibaRenderer::new);
	}
	
}
