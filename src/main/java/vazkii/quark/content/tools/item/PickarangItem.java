package vazkii.quark.content.tools.item;

import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.handler.QuarkSounds;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.entity.PickarangEntity;
import vazkii.quark.content.tools.module.PickarangModule;

import net.minecraft.world.item.Item.Properties;

public class PickarangItem extends QuarkItem {

	public final boolean isNetherite;
	
	public PickarangItem(String regname, QuarkModule module, Properties properties, boolean isNetherite) {
		super(regname, module, properties);
		this.isNetherite = isNetherite;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.hurtAndBreak(2, attacker, (player) -> player.broadcastBreakEvent(InteractionHand.MAIN_HAND));
		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState blockIn) {
		switch (isNetherite ? PickarangModule.netheriteHarvestLevel : PickarangModule.harvestLevel) {
			case 0:
				return Items.WOODEN_PICKAXE.isCorrectToolForDrops(blockIn) ||
						Items.WOODEN_AXE.isCorrectToolForDrops(blockIn) ||
						Items.WOODEN_SHOVEL.isCorrectToolForDrops(blockIn);
			case 1:
				return Items.STONE_PICKAXE.isCorrectToolForDrops(blockIn) ||
						Items.STONE_AXE.isCorrectToolForDrops(blockIn) ||
						Items.STONE_SHOVEL.isCorrectToolForDrops(blockIn);
			case 2:
				return Items.IRON_PICKAXE.isCorrectToolForDrops(blockIn) ||
						Items.IRON_AXE.isCorrectToolForDrops(blockIn) ||
						Items.IRON_SHOVEL.isCorrectToolForDrops(blockIn);
			default:
				return true;
		}
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return Math.max(isNetherite ? PickarangModule.netheriteDurability : PickarangModule.durability, 0);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, @Nonnull ToolType type, @Nullable Player player, @Nullable BlockState state) {
		return isNetherite ? PickarangModule.netheriteHarvestLevel : PickarangModule.harvestLevel;
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		if (state.getDestroySpeed(worldIn, pos) != 0)
			stack.hurtAndBreak(1, entityLiving, (player) -> player.broadcastBreakEvent(InteractionHand.MAIN_HAND));
		return true;
	}

	@Nonnull
	@Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, @Nonnull InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        playerIn.setItemInHand(handIn, ItemStack.EMPTY);
		int eff = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, itemstack);
		Vec3 pos = playerIn.position();
        worldIn.playSound(null, pos.x, pos.y, pos.z, QuarkSounds.ENTITY_PICKARANG_THROW, SoundSource.NEUTRAL, 0.5F + eff * 0.14F, 0.4F / (worldIn.random.nextFloat() * 0.4F + 0.8F));

        if(!worldIn.isClientSide)  {
        	int slot = handIn == InteractionHand.OFF_HAND ? playerIn.inventory.getContainerSize() - 1 : playerIn.inventory.selected;
        	PickarangEntity entity = new PickarangEntity(worldIn, playerIn);
        	entity.setThrowData(slot, itemstack, isNetherite);
        	entity.shoot(playerIn, playerIn.xRot, playerIn.yRot, 0.0F, 1.5F + eff * 0.325F, 0F);
            worldIn.addFreshEntity(entity);
        }

        if(!playerIn.abilities.instabuild && !PickarangModule.noCooldown) {
        	int cooldown = 10 - eff;
        	if (cooldown > 0)
				playerIn.getCooldowns().addCooldown(this, cooldown);
		}
        
        playerIn.awardStat(Stats.ITEM_USED.get(this));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }

	@Nonnull
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> multimap = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);

		if (slot == EquipmentSlot.MAINHAND) {
			multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 2, AttributeModifier.Operation.ADDITION)); 
			multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.8, AttributeModifier.Operation.ADDITION)); 
		}

		return multimap;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return 0F;
	}

	@Override
	public boolean isRepairable(ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return repair.getItem() == (isNetherite ? Items.NETHERITE_INGOT : Items.DIAMOND);
	}
	
	@Override
	public int getEnchantmentValue() {
		return isNetherite ? Items.NETHERITE_PICKAXE.getEnchantmentValue() : Items.DIAMOND_PICKAXE.getEnchantmentValue();
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(Enchantments.BLOCK_FORTUNE, Enchantments.SILK_TOUCH, Enchantments.BLOCK_EFFICIENCY).contains(enchantment);
	}
}
