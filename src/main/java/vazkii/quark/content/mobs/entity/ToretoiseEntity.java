package vazkii.quark.content.mobs.entity;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.handler.QuarkSounds;
import vazkii.quark.content.automation.module.IronRodModule;
import vazkii.quark.content.mobs.module.ToretoiseModule;

public class ToretoiseEntity extends Animal {

	public static final int ORE_TYPES = 4;
	public static final int ANGERY_TIME = 20; 

	private static final String TAG_TAMED = "tamed";
	private static final String TAG_ORE = "oreType";
	private static final String TAG_EAT_COOLDOWN = "eatCooldown";
	private static final String TAG_ANGERY_TICKS = "angeryTicks";

	public int rideTime;
	private boolean isTamed;
	private int eatCooldown;
	public int angeryTicks;

	private Ingredient goodFood;
	private LivingEntity lastAggressor;

	private static final EntityDataAccessor<Integer> ORE_TYPE = SynchedEntityData.defineId(ToretoiseEntity.class, EntityDataSerializers.INT);

	public ToretoiseEntity(EntityType<? extends ToretoiseEntity> type, Level world) {
		super(type, world);
		maxUpStep = 1.0F;
		setPathfindingMalus(BlockPathTypes.WATER, 1.0F);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

		entityData.define(ORE_TYPE, 0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new BreedGoal(this, 1.0));
		goalSelector.addGoal(1, new TemptGoal(this, 1.25, getGoodFood(), false));
		goalSelector.addGoal(2, new FollowParentGoal(this, 1.25));
		goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0D));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
	}
	
	private Ingredient getGoodFood() {
		if(goodFood == null)
			computeGoodFood();

		return goodFood;
	}

	private void computeGoodFood() {
		goodFood = Ingredient.of(ToretoiseModule.foods.stream()
				.map(loc -> Registry.ITEM.getOptional(new ResourceLocation(loc)))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(item -> new ItemStack(item)));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_213386_1_, DifficultyInstance p_213386_2_, MobSpawnType p_213386_3_, SpawnGroupData p_213386_4_, CompoundTag p_213386_5_) {
		popOre(true);
		return p_213386_4_;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	protected int decreaseAirSupply(int air) {
		return air;
	}

	@Override
	public boolean canBreed() {
		return getOreType() == 0 && eatCooldown == 0;
	}

	@Override
	public SoundEvent getEatingSound(ItemStack itemStackIn) {
		return null;
	}
//
//	@Override
//	public boolean isEntityInsideOpaqueBlock() {
//		return MiscUtil.isEntityInsideOpaqueBlock(this);
//	}

	@Override
	public void tick() {
		super.tick();

		AABB aabb = getBoundingBox();
		double rheight = getOreType() == 0 ? 1 : 1.4;
		aabb = new AABB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.minY + rheight, aabb.maxZ);
		setBoundingBox(aabb);

		Entity riding = getVehicle();
		if(riding != null)
			rideTime++;
		else rideTime = 0;

		if(eatCooldown > 0)
			eatCooldown--;

		if(angeryTicks > 0 && isAlive()) {
			angeryTicks--;

			if(onGround) {
				int dangerRange = 3;
				double x = getX() + getBbWidth() / 2;
				double y = getY();
				double z = getZ() + getBbWidth() / 2;

				if(level instanceof ServerLevel) {
					if(angeryTicks == 3)
						playSound(QuarkSounds.ENTITY_TORETOISE_ANGRY, 1F, 0.2F);
					else if(angeryTicks == 0) {
						((ServerLevel) level).sendParticles(ParticleTypes.CLOUD, x, y, z, 200, dangerRange, 0.5, dangerRange, 0);
					}
				}

				if(angeryTicks == 0) {
					AABB hurtAabb = new AABB(x - dangerRange, y - 1, z - dangerRange, x + dangerRange, y + 1, z + dangerRange);
					List<LivingEntity> hurtMeDaddy = level.getEntitiesOfClass(LivingEntity.class, hurtAabb, e -> !(e instanceof ToretoiseEntity));

					LivingEntity aggressor = lastAggressor == null ? this : lastAggressor;
					DamageSource damageSource = DamageSource.mobAttack(aggressor);
					for(LivingEntity e : hurtMeDaddy) {
						DamageSource useSource = damageSource;
						if(e == aggressor)
							useSource = DamageSource.mobAttack(this);
						
						e.hurt(useSource, 4 + level.getDifficulty().ordinal());
					}
				}
			}
		}

		int ore = getOreType();
		if(ore != 0) breakOre: {
			AABB ourBoundingBox = getBoundingBox();
			BlockPos min = new BlockPos(Math.round(ourBoundingBox.minX), Math.round(ourBoundingBox.minY), Math.round(ourBoundingBox.minZ));
			BlockPos max = new BlockPos(Math.round(ourBoundingBox.maxX), Math.round(ourBoundingBox.maxY), Math.round(ourBoundingBox.maxZ));

			for(int ix = min.getX(); ix <= max.getX(); ix++)
				for(int iy = min.getY(); iy <= max.getY(); iy++)
					for(int iz = min.getZ(); iz <= max.getZ(); iz++) {
						BlockPos test = new BlockPos(ix, iy, iz);
						BlockState state = level.getBlockState(test);
						if(state.getBlock() == Blocks.MOVING_PISTON) {
							BlockEntity tile = level.getBlockEntity(test);
							if(tile instanceof PistonMovingBlockEntity) {
								PistonMovingBlockEntity piston = (PistonMovingBlockEntity) tile;
								BlockState pistonState = piston.getMovedState();
								if(pistonState.getBlock() == IronRodModule.iron_rod) {
									dropOre(ore);
									break breakOre;
								}
							}
						}
					}
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		Entity e = source.getDirectEntity();
		int ore = getOreType();

		if(e instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) e;
			ItemStack held = living.getMainHandItem();

			if(ore != 0 && held.getItem().getToolTypes(held).contains(ToolType.PICKAXE)) {
				if(!level.isClientSide) {
					if(held.isDamageableItem() && e instanceof Player)
						MiscUtil.damageStack((Player) e, InteractionHand.MAIN_HAND, held, 1);

					dropOre(ore);
				}

				return false;
			}

			if(angeryTicks == 0) {
				angeryTicks = ANGERY_TIME;
				lastAggressor = living;
			}
		}

		return super.hurt(source, amount);
	}

	public void dropOre(int ore) {
		playSound(QuarkSounds.ENTITY_TORETOISE_HARVEST, 1F, 0.6F);

		Item drop = null;
		int countMult = 1;
		switch(ore) {
		case 1: 
			drop = Items.COAL;
			break;
		case 2:
			drop = Items.IRON_NUGGET;
			countMult *= 9;
			break;
		case 3:
			drop = Items.REDSTONE;
			countMult *= 3;
			break;
		case 4:
			drop = Items.LAPIS_LAZULI;
			countMult *= 2;
			break;
		}

		if(drop != null) {
			int count = 1;
			while(random.nextBoolean())
				count++;
			count *= countMult;

			spawnAtLocation(new ItemStack(drop, count), 1.2F);
		}

		entityData.set(ORE_TYPE, 0);
	}

	@Override
	public void setInLove(Player player) {
		setInLoveTime(0);
	}

	@Override
	public void setInLoveTime(int ticks) {
		if(level.isClientSide)
			return;

		playSound(QuarkSounds.ENTITY_TORETOISE_EAT, 0.5F + 0.5F * level.random.nextInt(2), (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
		heal(8);

		if(!isTamed) {
			isTamed = true;

			if(level instanceof ServerLevel)
				((ServerLevel) level).sendParticles(ParticleTypes.HEART, getX(), getY(), getZ(), 20, 0.5, 0.5, 0.5, 0);
		} else if (eatCooldown == 0) {
			popOre(false);
		}
	}

	private void popOre(boolean natural) {
		if (!natural && ToretoiseModule.regrowChance == 0)
			return;
		if(getOreType() == 0 && (natural || level.random.nextInt(ToretoiseModule.regrowChance) == 0)) {
			int ore = random.nextInt(ORE_TYPES) + 1;
			entityData.set(ORE_TYPE, ore);

			if(!natural) {
				eatCooldown = ToretoiseModule.cooldownTicks;

				if(level instanceof ServerLevel) {
					((ServerLevel) level).sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 100, 0.6, 0.6, 0.6, 0);
					playSound(QuarkSounds.ENTITY_TORETOISE_REGROW, 10F, 0.7F);
				}
			}
		}
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return getGoodFood().test(stack);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return !isTamed;
	}

	public static boolean spawnPredicate(EntityType<? extends ToretoiseEntity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, Random rand) {
		return world.getDifficulty() != Difficulty.PEACEFUL && pos.getY() <= ToretoiseModule.maxYLevel && MiscUtil.validSpawnLight(world, pos, rand) && MiscUtil.validSpawnLocation(type, world, reason, pos);
	}

	@Override
	public boolean checkSpawnRules(@Nonnull LevelAccessor world, MobSpawnType reason) {
		BlockState state = world.getBlockState((new BlockPos(position())).below());
		if (state.getMaterial() != Material.STONE)
			return false;

		return ToretoiseModule.dimensions.canSpawnHere(world);
	}

	@Override
	protected void jumpFromGround() {
		// NO-OP
	}

	@Override
	public boolean causeFallDamage(float distance, float damageMultiplier) {
		return false;
	}

	@Override
	protected float getWaterSlowDown() {
		return 0.9F;
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return false;
	}

	@Override
	protected float getVoicePitch() {
		return (random.nextFloat() - random.nextFloat()) * 0.2F + 0.6F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return QuarkSounds.ENTITY_TORETOISE_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return QuarkSounds.ENTITY_TORETOISE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return QuarkSounds.ENTITY_TORETOISE_DIE;
	}

	public int getOreType() {
		return entityData.get(ORE_TYPE);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putBoolean(TAG_TAMED, isTamed);
		compound.putInt(TAG_ORE, getOreType());
		compound.putInt(TAG_EAT_COOLDOWN, eatCooldown);
		compound.putInt(TAG_ANGERY_TICKS, angeryTicks);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		isTamed = compound.getBoolean(TAG_TAMED);
		entityData.set(ORE_TYPE, compound.getInt(TAG_ORE));
		eatCooldown = compound.getInt(TAG_EAT_COOLDOWN);
		angeryTicks = compound.getInt(TAG_ANGERY_TICKS);
	}

    public static AttributeSupplier.Builder prepareAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.08D) 
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

	@Override // createChild
	public ToretoiseEntity getBreedOffspring(ServerLevel sworld, AgableMob otherParent) {
		ToretoiseEntity e = new ToretoiseEntity(ToretoiseModule.toretoiseType, level);
		e.remove(); // kill the entity cuz toretoise doesn't make babies
		return e;
	}

}