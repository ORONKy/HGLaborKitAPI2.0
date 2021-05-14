package de.hglabor.plugins.kitapi.kit.kits;

import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.events.KitEvent;
import de.hglabor.plugins.kitapi.kit.settings.FloatArg;
import de.hglabor.plugins.kitapi.kit.settings.IntArg;
import de.hglabor.plugins.kitapi.kit.settings.MaterialArg;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class TrumpKit extends AbstractKit
{
	public static final TrumpKit INSTANCE = new TrumpKit();

	@IntArg
	private final int with, height,distance,time;

	@MaterialArg
	private final Material wallBlock;

	@FloatArg
	private final float cooldown;

	private final String trumpSnowballKey;

	public TrumpKit ( )
	{
		super( "Trump", Material.BRICKS );
		cooldown = 45;
		with = 4;
		height = 2;
		distance = 3;
		time = 5;
		wallBlock = Material.BRICKS;
		trumpSnowballKey = this.getName() + "trumpSnowBall";
		setMainKitItem( Material.BRICKS );
	}

	@KitEvent
	@Override
	public void onPlayerRightClickKitItem ( PlayerInteractEvent event )
	{
		super.onPlayerRightClickKitItem( event );
		Player player = event.getPlayer();
		player.sendMessage( "hey" );
		createWall( player );
	}

	private void createWall(Player player){
		World world = player.getWorld();
		Location origin = player.getEyeLocation();
		origin.setPitch( 0 );
		Vector direction = origin.getDirection();
		origin.add( direction.multiply( distance ) );
		Location centerLocation = origin.clone().add( direction );
		Location rotated =origin.clone();
		rotated.setPitch( 0 );
		rotated.setYaw( origin.getYaw() - 90 );
		Vector rotation = rotated.getDirection();
		rotation.normalize();
		Location blockLocation = centerLocation.clone().add( rotation ).subtract( 0, height/ 2, 0 );
		rotation.multiply( -1 );
		int initialX = blockLocation.getBlockX();
		int initialZ = blockLocation.getBlockZ();
		int initialY = blockLocation.getBlockY();
		List<Block> tempLocations = new ArrayList<>();
		List<Location> firstLayer = new ArrayList<>();
		for ( int i = 0; i < height; i++ )
		{
			for ( int j = 0; j < with; j++ )
			{
				tempLocations.add( blockLocation.getBlock() );
				blockLocation.add(rotation);
			}
			blockLocation.setY( initialY + i+1 );
			blockLocation.setX( initialX );
			blockLocation.setZ( initialZ );
		}
		for (Block block : tempLocations){
			if ( block.getType().equals( Material.AIR ) )
				block.setType( Material.BRICKS );
		}

		Bukkit.getScheduler().runTaskLater( KitApi.getInstance().getPlugin(), () -> {
			player.sendMessage( "scheduler: "+ tempLocations.size() );
			for ( Block block : tempLocations ){
				if ( block.getType().equals( Material.BRICKS ) ){
					block.setType( Material.AIR );
					world.spawnParticle( Particle.ASH, block.getLocation().clone().add( 0, 1, 0 ), 20, 0.3, 0.5, 0.3, 3);
				}
			}
		}, 20*time );

	}

	@Override
	public float getCooldown ()
	{
		return cooldown;
	}
}
