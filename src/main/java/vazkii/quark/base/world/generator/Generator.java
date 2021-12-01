package vazkii.quark.base.world.generator;

import java.util.Random;
import java.util.function.BooleanSupplier;

import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.server.level.WorldGenRegion;
import vazkii.quark.base.module.config.type.DimensionConfig;

public abstract class Generator implements IGenerator {
	
	public static final BooleanSupplier NO_COND = () -> true;
	
	public final DimensionConfig dimConfig;
	private final BooleanSupplier condition;
	
	public Generator(DimensionConfig dimConfig) {
		this(dimConfig, NO_COND);
	}
	
	public Generator(DimensionConfig dimConfig, BooleanSupplier condition) {
		this.dimConfig = dimConfig;
		this.condition = condition;
	}

	@Override
	public final int generate(int seedIncrement, long seed, GenerationStep.Decoration stage, WorldGenRegion worldIn, ChunkGenerator generator, WorldgenRandom rand, BlockPos pos) {
		rand.setFeatureSeed(seed, seedIncrement, stage.ordinal());
		generateChunk(worldIn, generator, rand, pos);
		return seedIncrement + 1;
	}

	public abstract void generateChunk(WorldGenRegion worldIn, ChunkGenerator generator, Random rand, BlockPos pos);

	@Override
	public boolean canGenerate(ServerLevelAccessor world) {
		return condition.getAsBoolean() && dimConfig.canSpawnHere(world.getLevel());
	}
	
	public Biome getBiome(LevelAccessor world, BlockPos pos, boolean offset) {
		// Move the position over to the top of the world to ensure it doesn't clip into potential
		// mod-added underground biomes
		
		BlockPos testPos = offset ? new BlockPos(pos.getX(), world.getMaxBuildHeight() - 1, pos.getZ()) : pos;
		return world.getBiomeManager().getBiome(testPos);
	}
	
	protected boolean isNether(LevelAccessor world) {
		return world.dimensionType().ultraWarm();
	}
	
}
