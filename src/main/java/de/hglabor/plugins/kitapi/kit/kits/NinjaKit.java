package de.hglabor.plugins.kitapi.kit.kits;

import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.KitManager;
import de.hglabor.plugins.kitapi.player.KitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Collections;

public class NinjaKit extends AbstractKit {
    private final static NinjaKit instance = new NinjaKit();

    private NinjaKit() {
        super("Ninja", Material.INK_SAC, 13);
        addEvents(Collections.singletonList(PlayerToggleSneakEvent.class));
    }

    public static NinjaKit getInstance() {
        return instance;
    }

    @Override
    public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        KitPlayer attacker = KitManager.getInstance().getPlayer(player);
        if (attacker == null || attacker.getLastHitInformation() == null || attacker.getLastHitInformation().getLastPlayer() == null)
            return;
        KitPlayer lastHittedPlayer = KitManager.getInstance().getPlayer(attacker.getLastHitInformation().getLastPlayer());
        if (lastHittedPlayer == null) {
            return;
        }
        Player toTeleport = Bukkit.getPlayer(lastHittedPlayer.getUUID());
        if (toTeleport != null) {
            if (!toTeleport.isOnline()) return;
            if (!lastHittedPlayer.isValid()) return;
            if (attacker.getLastHitInformation().getPlayerTimeStamp() + this.getCooldown() * 1000L > System.currentTimeMillis()) {
                if (distanceBetweenPlayers(player, toTeleport) < 30) {
                    player.teleport(calculateNinjaBehind(toTeleport));
                    attacker.activateKitCooldown(this, this.getCooldown());
                    attacker.getLastHitInformation().setPlayerTimeStamp(0);
                    attacker.getLastHitInformation().setLastPlayer(null);
                }
            }
        }
    }

    private Location calculateNinjaBehind(Entity entity) {
        float nang = entity.getLocation().getYaw() + 90;
        if (nang < 0) nang += 360;
        double nX = Math.cos(Math.toRadians(nang));
        double nZ = Math.sin(Math.toRadians(nang));
        return entity.getLocation().clone().subtract(nX, 0, nZ);
    }

    private int distanceBetweenPlayers(Player player, Entity entity) {
        Location ninjaLocation = player.getLocation().clone();
        Location entityLocation = entity.getLocation().clone();
        ninjaLocation.setY(0);
        entityLocation.setY(0);
        return (int) ninjaLocation.distance(entityLocation);
    }
}
