package vazkii.quark.base.capability;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.quark.addons.oddities.capability.MagnetTracker;
import vazkii.quark.api.ICustomSorting;
import vazkii.quark.api.IMagnetTracker;
import vazkii.quark.api.IPistonCallback;
import vazkii.quark.api.IRuneColorProvider;
import vazkii.quark.api.ITransferManager;
import vazkii.quark.api.QuarkCapabilities;
import vazkii.quark.base.Quark;
import vazkii.quark.base.capability.dummy.DummyMagnetTracker;
import vazkii.quark.base.capability.dummy.DummyPistonCallback;
import vazkii.quark.base.capability.dummy.DummyRuneColor;
import vazkii.quark.base.capability.dummy.DummySorting;

@Mod.EventBusSubscriber(modid = Quark.MOD_ID)
public class CapabilityHandler {

	public static void setup() {
		registerLambda(ITransferManager.class, (player) -> false);

		register(ICustomSorting.class, DummySorting::new);
		register(IPistonCallback.class, DummyPistonCallback::new);
		register(IMagnetTracker.class, DummyMagnetTracker::new);
		register(IRuneColorProvider.class, DummyRuneColor::new);
	}

	private static <T> void registerLambda(Class<T> clazz, T provider) {
		register(clazz, () -> provider);
	}

	private static <T> void register(Class<T> clazz, Callable<T> provider) {
		CapabilityManager.INSTANCE.register(clazz, new CapabilityFactory<>(), provider);
	}

	private static class CapabilityFactory<T> implements Capability.IStorage<T> {

		@Override
		public Tag writeNBT(Capability<T> capability, T instance, Direction side) {
			if (instance instanceof INBTSerializable)
				return ((INBTSerializable<?>) instance).serializeNBT();
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void readNBT(Capability<T> capability, T instance, Direction side, Tag nbt) {
			if (nbt instanceof CompoundTag)
				((INBTSerializable<Tag>) instance).deserializeNBT(nbt);
		}

	}

	private static final ResourceLocation DROPOFF_MANAGER = new ResourceLocation(Quark.MOD_ID, "dropoff");
	private static final ResourceLocation SORTING_HANDLER = new ResourceLocation(Quark.MOD_ID, "sort");
    private static final ResourceLocation MAGNET_TRACKER = new ResourceLocation(Quark.MOD_ID, "magnet_tracker");
    private static final ResourceLocation RUNE_COLOR_HANDLER = new ResourceLocation(Quark.MOD_ID, "rune_color");

	@SubscribeEvent
	public static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		Item item = event.getObject().getItem();

		if(item instanceof ICustomSorting)
			SelfProvider.attachItem(SORTING_HANDLER, QuarkCapabilities.SORTING, event);

		if(item instanceof IRuneColorProvider)
			SelfProvider.attachItem(RUNE_COLOR_HANDLER, QuarkCapabilities.RUNE_COLOR, event);
	}

	@SubscribeEvent
	public static void attachTileCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
		if (event.getObject() instanceof ITransferManager)
			SelfProvider.attach(DROPOFF_MANAGER, QuarkCapabilities.TRANSFER, event);
	}
	
    @SubscribeEvent
    public static void attachWorldCapabilities(AttachCapabilitiesEvent<Level> event) {
        Level world = event.getObject();
        MagnetTracker tracker = new MagnetTracker(world);

        event.addCapability(MAGNET_TRACKER, new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return QuarkCapabilities.MAGNET_TRACKER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> tracker));
            }
        });
    }
}
