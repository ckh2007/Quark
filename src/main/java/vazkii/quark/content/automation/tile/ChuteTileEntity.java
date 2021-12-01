package vazkii.quark.content.automation.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import vazkii.quark.content.automation.block.ChuteBlock;
import vazkii.quark.content.automation.module.ChuteModule;

/**
 * @author WireSegal
 * Created at 10:18 AM on 9/29/19.
 */
public class ChuteTileEntity extends BlockEntity {
    public ChuteTileEntity() {
        super(ChuteModule.tileEntityType);
    }

    private boolean canDropItem() {
        if(level != null && level.getBlockState(worldPosition).getValue(ChuteBlock.ENABLED)) {
            BlockPos below = worldPosition.below();
            BlockState state = level.getBlockState(below);
            Block block = state.getBlock();
            return block.isAir(state, level, below) || state.getCollisionShape(level, below).isEmpty();
        }

        return false;
    }

    private final IItemHandler handler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!canDropItem())
                return stack;

            if(!simulate && level != null && !stack.isEmpty()) {
                ItemEntity entity = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() - 0.5, worldPosition.getZ() + 0.5, stack.copy());
                entity.setDeltaMovement(0, 0, 0);
                level.addFreshEntity(entity);
            }

            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    };

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (side != Direction.DOWN && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.of(() -> handler).cast();
        return super.getCapability(cap, side);
    }
}
