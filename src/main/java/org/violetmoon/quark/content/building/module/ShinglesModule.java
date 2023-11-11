package org.violetmoon.quark.content.building.module;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.violetmoon.quark.base.handler.VariantHandler;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "building")
public class ShinglesModule extends ZetaModule {

	@LoadEvent
	public final void register(ZRegister event) {
		VariantHandler.addSlabAndStairs(new ZetaBlock("shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.TERRACOTTA)));

		VariantHandler.addSlabAndStairs(new ZetaBlock("white_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.WHITE_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("orange_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.ORANGE_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("magenta_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.MAGENTA_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("light_blue_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.LIGHT_BLUE_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("yellow_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.YELLOW_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("lime_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.LIME_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("pink_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.PINK_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("gray_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.GRAY_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("light_gray_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.LIGHT_GRAY_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("cyan_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.CYAN_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("purple_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.PURPLE_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("blue_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.BLUE_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("brown_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.BROWN_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("green_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.GREEN_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("red_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.RED_TERRACOTTA)));
		VariantHandler.addSlabAndStairs(new ZetaBlock("black_shingles", this, CreativeModeTab.TAB_BUILDING_BLOCKS, Block.Properties.copy(Blocks.BLACK_TERRACOTTA)));
	}

}
