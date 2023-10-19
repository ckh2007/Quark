package vazkii.quark.content.tweaks.module;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.zeta.module.ZetaModule;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = "tweaks", hasSubscriptions = true)
public class PigLittersModule extends ZetaModule {

	private static final String GOLDEN_CARROT_TAG = "quark:AteGoldenCarrot";

	@Config
	@Config.Min(1)
	public static int minPigLitterSize = 2;
	@Config
	@Config.Min(1)
	public static int maxPigLitterSize = 3;

	@Config
	public static boolean pigsEatGoldenCarrots = true;

	@Config
	@Config.Min(0)
	public static int minGoldenCarrotBoost = 0;
	@Config
	@Config.Min(0)
	public static int maxGoldenCarrotBoost = 2;

	public static boolean canEat(ItemStack stack) {
		return ModuleLoader.INSTANCE.isModuleEnabled(PigLittersModule.class) && pigsEatGoldenCarrots && stack.is(Items.GOLDEN_CARROT);
	}

	public static void onEat(Animal animal, ItemStack stack) {
		if (animal instanceof Pig && canEat(stack))
			animal.getPersistentData().putBoolean(GOLDEN_CARROT_TAG, true);
	}

	private static int getNumberBetween(RandomSource random, int boundA, int boundB) {
		int min = Math.min(boundA, boundB);
		int max = Math.max(boundA, boundB);

		return min + random.nextInt(max - min + 1);
	}

	@SubscribeEvent
	public void onPigAppear(EntityJoinLevelEvent event) {
		if (pigsEatGoldenCarrots && event.getEntity() instanceof Pig pig) {
			boolean alreadySetUp = pig.goalSelector.getAvailableGoals().stream()
					.anyMatch(goal -> goal.getGoal() instanceof TemptGoal tempt && tempt.items.test(new ItemStack(Items.GOLDEN_CARROT)));

			if (!alreadySetUp) {
				int priority = pig.goalSelector.getAvailableGoals().stream()
						.filter(goal -> goal.getGoal() instanceof TemptGoal)
						.findFirst()
						.map(WrappedGoal::getPriority)
						.orElse(-1);

				if (priority >= 0)
					MiscUtil.addGoalJustAfterLatestWithPriority(pig.goalSelector, 4, new TemptGoal(pig, 1.2D, Ingredient.of(Items.GOLDEN_CARROT), false));
			}
		}
	}

	@SubscribeEvent
	public void onEntityUpdate(LivingEvent.LivingTickEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity instanceof Animal animal && !animal.isInLove())
			animal.getPersistentData().remove(GOLDEN_CARROT_TAG);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPigBreed(BabyEntitySpawnEvent event) {
		AgeableMob mob = event.getChild();
		Mob mobA = event.getParentA();
		Mob mobB = event.getParentB();
		if (mob instanceof Pig) {
			Level lvl = mob.getLevel();
			if (lvl instanceof ServerLevel level &&
					mobA instanceof Animal parentA &&
					mobB instanceof Animal parentB){
				int litterSize = getNumberBetween(level.random, minPigLitterSize, maxPigLitterSize);

				if (mobA.getPersistentData().getBoolean(GOLDEN_CARROT_TAG))
					litterSize += getNumberBetween(level.random, minGoldenCarrotBoost, maxGoldenCarrotBoost);

				if (mobB.getPersistentData().getBoolean(GOLDEN_CARROT_TAG))
					litterSize += getNumberBetween(level.random, minGoldenCarrotBoost, maxGoldenCarrotBoost);

				if (litterSize > 1) {
					for (int i = 1; i < litterSize; i++) {
						AgeableMob newChild = parentA.getBreedOffspring(level, parentB);
						if (newChild != null) {
							Player cause = event.getCausedByPlayer();
							if (cause instanceof ServerPlayer player) {
								player.awardStat(Stats.ANIMALS_BRED);
								CriteriaTriggers.BRED_ANIMALS.trigger(player, parentA, parentB, newChild);
							}

							newChild.setBaby(true);
							newChild.moveTo(parentA.getX(), parentA.getY(), parentA.getZ(), 0.0F, 0.0F);
							level.addFreshEntityWithPassengers(newChild);
						}
					}
				}
			}
		}
	}
}
