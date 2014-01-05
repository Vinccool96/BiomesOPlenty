package biomesoplenty.common.eventhandler.world;

import static net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.FLOWERS;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import biomesoplenty.common.world.WorldGenFieldAssociation;
import biomesoplenty.common.world.decoration.BOPWorldFeatures;
import biomesoplenty.common.world.decoration.ForcedDecorators;
import biomesoplenty.common.world.decoration.IBOPDecoration;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class DecorateBiomeEventHandler 
{
	@SubscribeEvent
	public void onBiomeDecorate(DecorateBiomeEvent.Post event)
	{
		World world = event.world;

		int chunkX = event.chunkX;
		int chunkZ = event.chunkZ;

		Random random = event.rand;
		
		int x = chunkX + 8;
		int z = chunkZ + 8;

		BiomeGenBase biome = world.getBiomeGenForCoordsBody(x, z);
		
		IBOPDecoration bopDecoration = null;
		
		if (biome instanceof IBOPDecoration)
		{
			bopDecoration = (IBOPDecoration)biome;
		}
		else if (ForcedDecorators.biomeHasForcedDecorator(biome.biomeID))
		{
			bopDecoration = ForcedDecorators.getForcedDecorator(biome.biomeID);
		}

		if (bopDecoration != null)
		{
			for (Field worldGeneratorField : BOPWorldFeatures.PerChunk.class.getFields())
			{
				try
				{
					int worldGenPerChunk = worldGeneratorField.getInt(bopDecoration.getWorldFeatures().perChunk);

					for (int i = 0; i < worldGenPerChunk; i++)
					{
						int randX = x + random.nextInt(16);
						int randZ = z + random.nextInt(16);

						WorldGenerator worldGenerator = null;

						if (worldGeneratorField.getName().equals("bopFlowersPerChunk") && TerrainGen.decorate(world, random, chunkX, chunkZ, FLOWERS))
						{
							worldGenerator = bopDecoration.getRandomWorldGenForBOPFlowers(random);
						}
						else
						{
							worldGenerator = WorldGenFieldAssociation.getAssociatedWorldGenerator(worldGeneratorField.getName());
						}

						if (worldGenerator != null)
						{
							worldGenerator.generate(world, random, randX, world.getTopSolidOrLiquidBlock(randX, randZ), randZ);
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void modifyDecor(Decorate event)
	{
		World world = event.world;

		int chunkX = event.chunkX;
		int chunkZ = event.chunkZ;

		Random random = event.rand;
		
		int x = chunkX + 8;
		int z = chunkZ + 8;
		
		
		BiomeGenBase biome = world.getBiomeGenForCoordsBody(x, z);
		IBOPDecoration bopDecoration = null;
		
		if (biome instanceof IBOPDecoration)
		{
			bopDecoration = (IBOPDecoration)biome;
		}
		else if (ForcedDecorators.biomeHasForcedDecorator(biome.biomeID))
		{
			bopDecoration = ForcedDecorators.getForcedDecorator(biome.biomeID);
		}
		
		if (bopDecoration != null)
		{
			if (event.type == EventType.PUMPKIN)
			{
				if (!bopDecoration.getWorldFeatures().doGeneration.generatePumpkins) event.setCanceled(true);
			}
		}
	}
	
	public static void decorate(World world, Random random, BiomeGenBase biome, int x, int z)
	{
		BiomeDecorator biomeDecorator = biome.theBiomeDecorator;
		
        if (biomeDecorator.currentWorld != null)
        {
            return;
        }
        else
        {
            biomeDecorator.currentWorld = world;
            biomeDecorator.randomGenerator = random;
            biomeDecorator.chunk_X = x;
            biomeDecorator.chunk_Z = z;
            
            //TODO:			decorate
            try
            {
            	ReflectionHelper.findMethod(BiomeDecorator.class, biomeDecorator, new String[] { "func_150513_a" }, BiomeGenBase.class).invoke(biomeDecorator, biome);
            }
            catch (Exception e)
            {

            }
            
            biomeDecorator.currentWorld = null;
            biomeDecorator.randomGenerator = null;
        }
	}
}