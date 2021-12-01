package vazkii.quark.addons.oddities.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import vazkii.quark.addons.oddities.block.MagnetBlock;
import vazkii.quark.addons.oddities.magnetsystem.MagnetSystem;
import vazkii.quark.addons.oddities.module.MagnetsModule;

public class MagnetTileEntity extends BlockEntity implements TickableBlockEntity {

	public MagnetTileEntity() {
		super(MagnetsModule.magnetType);
	}

	@Override
	public void tick() {
		BlockState state = getBlockState();
		boolean powered = state.getValue(MagnetBlock.POWERED);

		if(powered) {
			Direction dir = state.getValue(MagnetBlock.FACING);
			int power = getPower(dir);
			magnetize(dir, dir, power);
			magnetize(dir.getOpposite(), dir, power);
		}
	}

	private void magnetize(Direction dir, Direction moveDir, int power) {
		if (level == null)
			return;

		double magnitude = (dir == moveDir ? 1 : -1);

		double particleMotion = 0.05 * magnitude;
		double particleChance = 0.2;
		double xOff = dir.getStepX() * particleMotion;
		double yOff = dir.getStepY() * particleMotion;
		double zOff = dir.getStepZ() * particleMotion;

		for(int i = 1; i <= power; i++) {
			BlockPos targetPos = worldPosition.relative(dir, i);
			BlockState targetState = level.getBlockState(targetPos);

			if (targetState.getBlock() == MagnetsModule.magnetized_block)
				break;

			if(!level.isClientSide && targetState.getBlock() != Blocks.MOVING_PISTON && targetState.getBlock() != MagnetsModule.magnetized_block) {
				PushReaction reaction = MagnetSystem.getPushAction(this, targetPos, targetState, moveDir);
				if (reaction == PushReaction.IGNORE || reaction == PushReaction.DESTROY) {
					BlockPos frontPos = targetPos.relative(moveDir);
					BlockState frontState = level.getBlockState(frontPos);
					if(frontState.isAir(level, frontPos))
						MagnetSystem.applyForce(level, targetPos, power - i + 1, dir == moveDir, moveDir, i, worldPosition);
				}
			}

			if(!targetState.isAir(level, targetPos))
				break;

			if (level.isClientSide && Math.random() <= particleChance) {
				double x = targetPos.getX() + (xOff == 0 ? 0.5 : Math.random());
				double y = targetPos.getY() + (yOff == 0 ? 0.5 : Math.random());
				double z = targetPos.getZ() + (zOff == 0 ? 0.5 : Math.random());
				level.addParticle(ParticleTypes.SNEEZE, x, y, z, xOff, yOff, zOff);
			}
		}
	}

	private int getPower(Direction curr) {
		if (level == null)
			return 0;

		int power = 0;
		Direction opp = curr.getOpposite();
		
		for(Direction dir : Direction.values()) {
			if(dir != opp && dir != curr) {
				int offPower = level.getSignal(worldPosition.relative(dir), dir);
				power = Math.max(offPower, power);
			}
		}
		
		return power;
	}

}
