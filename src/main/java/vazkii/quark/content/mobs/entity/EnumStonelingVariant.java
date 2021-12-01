/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 23, 2019, 16:18 AM (EST)]
 */
package vazkii.quark.content.mobs.entity;

import static vazkii.quark.content.world.module.NewStoneTypesModule.jasperBlock;
import static vazkii.quark.content.world.module.NewStoneTypesModule.limestoneBlock;
import static vazkii.quark.content.world.module.NewStoneTypesModule.marbleBlock;
import static vazkii.quark.content.world.module.NewStoneTypesModule.polishedBlocks;
import static vazkii.quark.content.world.module.NewStoneTypesModule.slateBlock;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import vazkii.quark.base.Quark;

public enum EnumStonelingVariant implements SpawnGroupData {
	STONE("stone", Blocks.COBBLESTONE, Blocks.STONE),
	ANDESITE("andesite", Blocks.ANDESITE, Blocks.POLISHED_ANDESITE),
	DIORITE("diorite", Blocks.DIORITE, Blocks.POLISHED_DIORITE),
	GRANITE("granite", Blocks.GRANITE, Blocks.POLISHED_GRANITE),
	LIMESTONE("limestone", limestoneBlock, polishedBlocks.get(limestoneBlock)),
	MARBLE("marble", marbleBlock, polishedBlocks.get(marbleBlock)),
	SLATE("slate", slateBlock, polishedBlocks.get(slateBlock)),
	JASPER("jasper", jasperBlock, polishedBlocks.get(jasperBlock));

	private final ResourceLocation texture;
	private final List<Block> blocks;

	EnumStonelingVariant(String variantPath, Block... blocks) {
		this.texture = new ResourceLocation(Quark.MOD_ID, "textures/model/entity/stoneling/" + variantPath + ".png");
		this.blocks = Lists.newArrayList(blocks);
	}

	public static EnumStonelingVariant byIndex(byte index) {
		EnumStonelingVariant[] values = values();
		return values[Mth.clamp(index, 0, values.length - 1)];
	}

	public byte getIndex() {
		return (byte) ordinal();
	}

	public ResourceLocation getTexture() {
		return texture;
	}

	public List<Block> getBlocks() {
		return blocks;
	}
}
