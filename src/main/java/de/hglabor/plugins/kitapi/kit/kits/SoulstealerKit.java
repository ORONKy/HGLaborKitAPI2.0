package de.hglabor.plugins.kitapi.kit.kits;

import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.events.KitEvent;
import de.hglabor.plugins.kitapi.kit.settings.IntArg;
import de.hglabor.plugins.kitapi.kit.settings.PotionEffectArg;
import de.hglabor.plugins.kitapi.player.KitPlayer;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SoulstealerKit extends AbstractKit implements Listener {
    public final static SoulstealerKit INSTANCE = new SoulstealerKit();
    private final String respawnKey, runnableKey;
    @PotionEffectArg
    private final PotionEffectType effectOnDeath;
    @IntArg
    private final int effectAmplifier, effectDuration;

    private SoulstealerKit() {
        super("Soulstealer", Material.BONE);
        this.respawnKey = this.getName() + "respawn";
        this.runnableKey = this.getName() + "deathTimer";
        this.effectOnDeath = PotionEffectType.SPEED;
        this.effectAmplifier = 2;
        this.effectDuration = 10;
    }

    @Override
    public void onDisable(KitPlayer kitPlayer) {
        DeathTimer deathTimer = kitPlayer.getKitAttribute(runnableKey);
        if (deathTimer != null)
            deathTimer.cancel();
    }

    @KitEvent
    @Override
    public void onKitPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        KitPlayer kitPlayer = KitApi.getInstance().getPlayer(player);
        if (!player.hasMetadata(respawnKey)) {
            DeathTimer deathTimer = new DeathTimer(kitPlayer);
            kitPlayer.putKitAttribute(runnableKey, deathTimer);
            deathTimer.runTaskTimer(KitApi.getInstance().getPlugin(), 0, 20);
            player.setMetadata(respawnKey, new FixedMetadataValue(KitApi.getInstance().getPlugin(), ""));
            player.setInvisible(true);
            player.addPotionEffect(new PotionEffect(effectOnDeath, effectDuration * 20, effectAmplifier));
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerKillsPlayer(KitPlayer killer, KitPlayer dead) {
        killer.getBukkitPlayer().ifPresent(player -> {
            if (player.hasMetadata(respawnKey)) {
                player.sendMessage(Localization.INSTANCE.getMessage("soulstealer.revived", ChatUtils.getPlayerLocale(killer.getUUID())));
                player.removeMetadata(respawnKey, KitApi.getInstance().getPlugin());
                player.setInvisible(false);
                player.removePotionEffect(effectOnDeath);
                DeathTimer deathTimer = killer.getKitAttribute(runnableKey);
                deathTimer.cancel();
            }
        });
    }

    private class DeathTimer extends BukkitRunnable {
        private final KitPlayer kitPlayer;
        private final BossBar bossBar;
        private int counter;

        DeathTimer(KitPlayer kitPlayer) {
            this.kitPlayer = kitPlayer;
            this.counter = effectDuration;
            this.bossBar = Bukkit.createBossBar(ChatColor.RED + "KILL A PLAYER TO RESPAWN", BarColor.WHITE, BarStyle.SOLID);
            this.init();
        }

        private void init() {
            bossBar.setProgress(1D);
            kitPlayer.getBukkitPlayer().ifPresent(bossBar::addPlayer);
        }

        @Override
        public void run() {
            if (isCancelled())
                return;
            if (!kitPlayer.isValid())
                cancel();
            if (counter == 0) {
                kitPlayer.getBukkitPlayer().ifPresent(player -> player.setHealth(0));
                cancel();
                return;
            }
            bossBar.setProgress((double) counter / effectDuration);
            counter--;
        }
    }
}
