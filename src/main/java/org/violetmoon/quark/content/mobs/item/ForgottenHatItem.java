package org.violetmoon.quark.content.mobs.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nonnull;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.item.IZetaItem;
import org.violetmoon.zeta.item.ZetaArmorItem;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.UUID;
import java.util.function.BooleanSupplier;

public class ForgottenHatItem extends ZetaArmorItem implements IZetaItem {

	private static final String TEXTURE = Quark.MOD_ID + ":textures/misc/forgotten_hat_worn.png";

	private final ZetaModule module;
	private Multimap<Attribute, AttributeModifier> attributes;

	public ForgottenHatItem(ZetaModule module) {
		super(ArmorMaterials.LEATHER, EquipmentSlot.HEAD,
				new Item.Properties()
				.stacksTo(1)
				.durability(0)
				.tab(CreativeModeTab.TAB_TOOLS)
				.rarity(Rarity.RARE));

		Quark.ZETA.registry.registerItem(this, "forgotten_hat");
		this.module = module;
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

	@Override
	public String getArmorTextureZeta(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return TEXTURE;
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		if(attributes == null) {
			Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			UUID uuid = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");
			builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", 1, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.LUCK, new AttributeModifier(uuid, "Armor luck modifier", 1, AttributeModifier.Operation.ADDITION));
			builder.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(uuid, "Armor reach modifier", 2, AttributeModifier.Operation.ADDITION));

			attributes = builder.build();
		}

		return slot == this.slot ? attributes : super.getDefaultAttributeModifiers(slot);
	}

	@Override
	public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items) {
		if(isEnabled() || group == CreativeModeTab.TAB_SEARCH)
			super.fillItemCategory(group, items);
	}

}
