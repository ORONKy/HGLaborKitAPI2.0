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

public class TrumpKit extends AbstractKit {
    public static final TrumpKit INSTANCE = new TrumpKit();

    @IntArg
    private final int width, height, distance, time;

    @FloatArg
    private final float cooldown;

    public TrumpKit() {
        super("Trump", Material.BRICKS);
        cooldown = 45;
        width = 4;
        height = 2;
        distance = 3;
        time = 5;
        setMainKitItem(Material.BRICKS);
    }

    @KitEvent
    @Override
    public void onPlayerRightClickKitItem(PlayerInteractEvent event) {
        super.onPlayerRightClickKitItem(event);
        Player player = event.getPlayer();
        createWall(player);
    }

    private void createWall(Player player) {
        World world = player.getWorld();
        Location origin = player.getEyeLocation();
        origin.setPitch(0);
        origin.add(0, -1, 0);

        Location startBlock = origin.clone();

        List<Block> tempLocations = new ArrayList<>();
        List<Location> firstLayer = getBaseLine(with, origin.clone());

        boolean isOnBlock = false;
        boolean isNotInBlock = false;
        for (int i = 0; i < 3; i++) {

            for (Location location : firstLayer) {

                if (!Material.AIR.equals(world.getBlockAt(location.clone().add(0, -1, 0)).getType())) {
                    isOnBlock = true;
                    break;
                }
            }
            if (!isOnBlock) {
                startBlock.setY((int) startBlock.getY());
                firstLayer = getBaseLine(with, startBlock.clone());
            } else {
                break;
            }
        }

        for (int i = 0; i < 4; i++) {
            for (Location location : firstLayer) {
                if (Material.AIR.equals(world.getBlockAt(location).getType())) {
                    isNotInBlock = true;
                    break;
                }

            }
            if (!isNotInBlock) {
                startBlock.setY(((int) startBlock.getY()) + 1);
                firstLayer = getBaseLine(with, startBlock.clone());
            } else {
                break;
            }
        }

        if (isOnBlock && isNotInBlock) {
            for (int i = 0; i < height; i++) {
                for (Location location : firstLayer) {
                    location.setY((int) location.getY() + i);
                    tempLocations.add(location.getBlock());
                }
            }
            for (Block block : tempLocations) {
                if (block.getType().equals(Material.AIR))
                    block.setType(Material.BRICKS);
            }

            Bukkit.getScheduler().runTaskLater(KitApi.getInstance().getPlugin(), () -> {
                for (Block block : tempLocations) {
                    if (block.getType().equals(Material.BRICKS)) {
                        block.setType(Material.AIR);
                        world.spawnParticle(Particle.ASH, block.getLocation().clone().add(0, 1, 0), 20, 0.3, 0.5,
                                0.3, 3);
                    }
                }
            }, 20 * time);
        } else {
            world.spawnParticle(Particle.ASH, player.getEyeLocation().clone().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 3);
        }
    }

    private List<Location> getBaseLine(int with, Location baseLocation) {
        List<Location> firstLayer = new ArrayList<>();

        Vector direction = baseLocation.clone().getDirection();
        baseLocation.add(direction.clone().normalize().multiply(distance));
        Location centerLocation = baseLocation.clone();
        Location rotated = baseLocation.clone();
        rotated.setPitch(0);
        rotated.setYaw(baseLocation.getYaw() - 90);
        Vector rotation = rotated.getDirection();
        rotation.normalize();
        Location blockLocation = centerLocation.clone().add(rotation).subtract(0, height / 2, 0);
        rotation.multiply(-1);

        for (int j = 0; j < with; j++) {
            Location location = blockLocation.clone();
            location.setX((int) location.getX());
            location.setY((int) location.getY());
            location.setZ((int) location.getZ());
            firstLayer.add(location);
            blockLocation.add(rotation);
        }
        return firstLayer;
    }

    @Override
    public float getCooldown() {
        return cooldown;
    }
}
