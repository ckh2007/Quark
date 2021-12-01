package vazkii.quark.content.tools.entity;

import javax.annotation.Nonnull;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.content.tools.module.SkullPikesModule;

public class SkullPikeEntity extends Entity {

	public SkullPikeEntity(EntityType<?> type, Level world) {
		super(type, world);
	}

	@Override
	public void tick() {
		super.tick();

		if(level instanceof ServerLevel) {
			boolean good = false;
			BlockPos pos = blockPosition();
			BlockState state = level.getBlockState(pos);

			if(state.getBlock().is(SkullPikesModule.pikeTrophiesTag)) {
				BlockPos down = pos.below();
				BlockState downState = level.getBlockState(down);

				if(downState.getBlock().is(BlockTags.FENCES))
					good = true;
			}

			if(!good)
				removeAfterChangingDimensions();

			ServerLevel sworld = (ServerLevel) level;
			if(Math.random() < 0.4)
				sworld.sendParticles(Math.random() < 0.05 ? ParticleTypes.WARPED_SPORE : ParticleTypes.ASH, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, 1, 0.25, 0.25, 0.25, 0);
		}
	}

	public boolean isVisible(Entity entityIn) {
		Vec3 vector3d = new Vec3(getX(), getY() + 1, getZ());
		Vec3 vector3d1 = new Vec3(entityIn.getX(), entityIn.getEyeY(), entityIn.getZ());
		return level.clip(new ClipContext(vector3d, vector3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
	}

	@Override
	protected void defineSynchedData() {
		// NO-OP
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		// NO-OP
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		// NO-OP
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
