package org.violetmoon.quark.addons.oddities.item;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.util.RequiredModTooltipHandler;
import org.violetmoon.quark.base.handler.ProxiedItemStackHandler;
import org.violetmoon.zeta.item.IZetaItem;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.ItemNBTHelper;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BackpackItem extends DyeableArmorItem implements IZetaItem, IZetaItemExtensions, MenuProvider {

	private static final String WORN_TEXTURE = Quark.MOD_ID + ":textures/misc/backpack_worn.png";
	private static final String WORN_OVERLAY_TEXTURE = Quark.MOD_ID + ":textures/misc/backpack_worn_overlay.png";

	private final ZetaModule module;

	public BackpackItem(ZetaModule module) {
		super(ArmorMaterials.LEATHER, EquipmentSlot.CHEST,
				new Item.Properties()
				.stacksTo(1)
				.durability(0)
				.tab(CreativeModeTab.TAB_TOOLS)
				.rarity(Rarity.RARE));

		this.module = module;
		module.zeta.registry.registerItem(this, "backpack");

		if(module.category.isAddon())
			module.zeta.requiredModTooltipHandler.map(this, module.category.requiredMod);
	}

	@Override
	public int getDefaultTooltipHideFlagsZeta(@Nonnull ItemStack stack) {
		return stack.isEnchanted() ? ItemStack.TooltipPart.ENCHANTMENTS.getMask() : 0;
	}

	@Override
	public ZetaModule getModule() {
		return module;
	}

	@Override
	public IZetaItem setCondition(BooleanSupplier condition) {
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return true;
	}

	public static boolean doesBackpackHaveItems(ItemStack stack) {
		LazyOptional<IItemHandler> handlerOpt = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);

		if (!handlerOpt.isPresent())
			return false;

		IItemHandler handler = handlerOpt.orElse(new ItemStackHandler());
		for(int i = 0; i < handler.getSlots(); i++)
			if(!handler.getStackInSlot(i).isEmpty())
				return true;

		return false;
	}

	@Override
	public boolean isBookEnchantableZeta(ItemStack stack, ItemStack book) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTableZeta(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public int getEnchantmentValueZeta(ItemStack stack) {
		return 0;
	}

	@Override
	public boolean canBeDepleted() {
		return false;
	}

	@Override
	public <T extends LivingEntity> int damageItemZeta(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		return 0;
	}

	@Override
	public void inventoryTick(@Nonnull ItemStack stack, Level worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
		if(worldIn.isClientSide)
			return;

		boolean hasItems = !BackpackModule.superOpMode && doesBackpackHaveItems(stack);

		Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
		boolean isCursed = enchants.containsKey(Enchantments.BINDING_CURSE);
		boolean changedEnchants = false;

		if(hasItems) {
			if(BackpackModule.isEntityWearingBackpack(entityIn, stack)) {
				if(!isCursed) {
					enchants.put(Enchantments.BINDING_CURSE, 1);
					changedEnchants = true;
				}

				if(BackpackModule.itemsInBackpackTick) {
					LazyOptional<IItemHandler> handlerOpt = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
					IItemHandler handler = handlerOpt.orElse(new ItemStackHandler());
					for(int i = 0; i < handler.getSlots(); i++) {
						ItemStack inStack = handler.getStackInSlot(i);
						if(!inStack.isEmpty())
							inStack.getItem().inventoryTick(inStack, worldIn, entityIn, i, false);
					}
				}
			} else {
				ItemStack copy = stack.copy();
				stack.setCount(0);
				entityIn.spawnAtLocation(copy, 0);
			}
		} else if(isCursed) {
			enchants.remove(Enchantments.BINDING_CURSE);
			changedEnchants = true;
		}

		if(changedEnchants)
			EnchantmentHelper.setEnchantments(enchants, stack);
	}

	@Override
	public boolean onEntityItemUpdateZeta(ItemStack stack, ItemEntity entityItem) {
		if(BackpackModule.superOpMode || entityItem.level.isClientSide)
			return false;

		if (!ItemNBTHelper.detectNBT(stack))
			return false;

		LazyOptional<IItemHandler> handlerOpt = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);

		if(!handlerOpt.isPresent())
			return false;

		IItemHandler handler = handlerOpt.orElse(new ItemStackHandler());

		for(int i = 0; i < handler.getSlots(); i++) {
			ItemStack stackAt = handler.getStackInSlot(i);
			if(!stackAt.isEmpty()) {
				ItemStack copy = stackAt.copy();
				Containers.dropItemStack(entityItem.level, entityItem.getX(), entityItem.getY(), entityItem.getZ(), copy);
			}
		}

		CompoundTag comp = ItemNBTHelper.getNBT(stack);
		comp.remove("Inventory");
		if (comp.size() == 0)
			stack.setTag(null);

		return false;
	}

	//TODO: IForgeItem
	@Nonnull
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag oldCapNbt) {
		ProxiedItemStackHandler handler = new ProxiedItemStackHandler(stack, 27);

		if (oldCapNbt != null && oldCapNbt.contains("Parent")) {
			CompoundTag itemData = oldCapNbt.getCompound("Parent");
			ItemStackHandler stacks = new ItemStackHandler();
			stacks.deserializeNBT(itemData);

			for (int i = 0; i < stacks.getSlots(); i++)
				handler.setStackInSlot(i, stacks.getStackInSlot(i));

			oldCapNbt.remove("Parent");
		}

		return handler;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot p_40390_) {
		return ImmutableMultimap.of();
	}

	@Override
	public String getArmorTextureZeta(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return type != null && type.equals("overlay") ? WORN_OVERLAY_TEXTURE : WORN_TEXTURE;
	}

	@Override
	public boolean isFoil(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items) {
		if(isEnabled() || group == CreativeModeTab.TAB_SEARCH)
			super.fillItemCategory(group, items);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inv, @Nonnull Player player) {
		return new BackpackMenu(id, player);
	}

	@Nonnull
	@Override
	public Component getDisplayName() {
		return Component.translatable(getDescriptionId());
	}


}
