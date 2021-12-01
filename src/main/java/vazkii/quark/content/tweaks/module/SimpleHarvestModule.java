/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 05, 2019, 16:56 AM (EST)]
 */
package vazkii.quark.content.tweaks.module;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.HarvestMessage;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class SimpleHarvestModule extends QuarkModule {

	@Config(description = "Can players harvest crops with empty hand clicks?")
	public static boolean emptyHandHarvest = true;
	@Config(description = "Does harvesting crops with a hoe cost durability?")
	public static boolean harvestingCostsDurability = false;
	@Config(description = "Should Quark look for (nonvanilla) crops, and handle them?")
	public static boolean doHarvestingSearch = true;

	@Config(description = "Which crops can be harvested?\n" +
			"Format is: \"harvestState[,afterHarvest]\", i.e. \"minecraft:wheat[age=7]\" or \"minecraft:cocoa[age=2,facing=north],minecraft:cocoa[age=0,facing=north]\"")
	public static List<String> harvestableBlocks = Lists.newArrayList(
			"minecraft:wheat[age=7]",
			"minecraft:carrots[age=7]",
			"minecraft:potatoes[age=7]",
			"minecraft:beetroots[age=3]",
			"minecraft:nether_wart[age=3]",
			"minecraft:cocoa[age=2,facing=north],minecraft:cocoa[age=0,facing=north]",
			"minecraft:cocoa[age=2,facing=south],minecraft:cocoa[age=0,facing=south]",
			"minecraft:cocoa[age=2,facing=east],minecraft:cocoa[age=0,facing=east]",
			"minecraft:cocoa[age=2,facing=west],minecraft:cocoa[age=0,facing=west]");

	public static final Map<BlockState, BlockState> crops = Maps.newHashMap();


	@Override
	public void configChanged() {
		crops.clear();

		if (doHarvestingSearch) {
			GameRegistry.findRegistry(Block.class).getValues().stream()
					.filter(b -> !isVanilla(b) && b instanceof CropBlock)
					.map(b -> (CropBlock) b)
					.forEach(b -> crops.put(b.defaultBlockState().setValue(b.getAgeProperty(), last(b.getAgeProperty().getPossibleValues())), b.defaultBlockState()));
		}

		for (String harvestKey : harvestableBlocks) {
			BlockState initial, result;
			String[] split = tokenize(harvestKey);
			initial = fromString(split[0]);
			if (split.length > 1)
				result = fromString(split[1]);
			else
				result = initial.getBlock().defaultBlockState();

			if (initial.getBlock() != Blocks.AIR)
				crops.put(initial, result);
		}
	}
	
	private int last(Collection<Integer> vals) {
		return vals.stream().max(Integer::compare).orElse(0);
	}

	private String[] tokenize(String harvestKey) {
		boolean inBracket = false;
		for (int i = 0; i < harvestKey.length(); i++) {
			char charAt = harvestKey.charAt(i);
			if (charAt == '[')
				inBracket = true;
			else if (charAt == ']')
				inBracket = false;
			else if (charAt == ',' && !inBracket)
				return new String[] { harvestKey.substring(0, i), harvestKey.substring(i + 1) };
		}
		return new String[] { harvestKey };
	}

	private boolean isVanilla(IForgeRegistryEntry<?> entry) {
		ResourceLocation loc = entry.getRegistryName();
		if (loc == null)
			return true; // Just in case

		return loc.getNamespace().equals("minecraft");
	}

	private BlockState fromString(String key) {
		try {
			BlockStateParser parser = new BlockStateParser(new StringReader(key), false).parse(false);
			BlockState state = parser.getState();
			return state == null ? Blocks.AIR.defaultBlockState() : state;
		} catch (CommandSyntaxException e) {
			return Blocks.AIR.defaultBlockState();
		}
	}

	private static void replant(Level world, BlockPos pos, BlockState inWorld, Player player) {
		ItemStack mainHand = player.getMainHandItem();
		boolean isHoe = !mainHand.isEmpty() && mainHand.getItem() instanceof HoeItem;

		BlockState newBlock = crops.get(inWorld);
		int fortune = HoeHarvestingModule.canFortuneApply(Enchantments.BLOCK_FORTUNE, mainHand) && isHoe ?
				EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, mainHand) : 0;

		ItemStack copy = mainHand.copy();
		if (copy.isEmpty())
			copy = new ItemStack(Items.STICK);

		Map<Enchantment, Integer> enchMap = EnchantmentHelper.getEnchantments(copy);
		enchMap.put(Enchantments.BLOCK_FORTUNE, fortune);
		EnchantmentHelper.setEnchantments(enchMap, copy);

		if (world instanceof ServerLevel) {
			Item blockItem = inWorld.getBlock().asItem();
	        Block.getDrops(inWorld, (ServerLevel) world, pos, world.getBlockEntity(pos), player, copy).forEach((stack) -> {
	        	if(stack.getItem() == blockItem)
	        		stack.shrink(1);
	        	
	        	if(!stack.isEmpty())
	        		Block.popResource(world, pos, stack);
	        });
	        inWorld.spawnAfterBreak((ServerLevel) world, pos, copy);

			if (!world.isClientSide) {
				world.levelEvent(2001, pos, Block.getId(newBlock));
				world.setBlockAndUpdate(pos, newBlock);
			}
		}
	}

	@SubscribeEvent
	public void onClick(PlayerInteractEvent.RightClickBlock event) {
		if (click(event.getPlayer(), event.getPos())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}

	public static boolean click(Player player, BlockPos pos) {
		if (player == null)
			return false;

		ItemStack mainHand = player.getMainHandItem();
		boolean isHoe = !mainHand.isEmpty() && mainHand.getItem() instanceof HoeItem;

		if (!emptyHandHarvest && !isHoe)
			return false;

		int range = HoeHarvestingModule.getRange(mainHand);

		int harvests = 0;

		for(int x = 1 - range; x < range; x++) {
			for (int z = 1 - range; z < range; z++) {
				BlockPos shiftPos = pos.offset(x, 0, z);

				BlockState worldBlock = player.level.getBlockState(shiftPos);
				if (crops.containsKey(worldBlock)) {
					replant(player.level, shiftPos, worldBlock, player);
					harvests++;
				}
			}
		}

		if (harvests > 0) {
			if (harvestingCostsDurability && isHoe && !player.level.isClientSide)
				mainHand.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));

			if (mainHand.isEmpty() && player.level.isClientSide)
				QuarkNetwork.sendToServer(new HarvestMessage(pos));
			return true;
		}

		return false;
	}
}
