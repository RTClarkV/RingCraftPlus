package org.randomlima.ringcraftplus.Listeners.AmogusRing;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.randomlima.ringcraftplus.Colorize;
import org.randomlima.ringcraftplus.CustomItems.CustomItems;
import org.randomlima.ringcraftplus.RingCraftPlus;

import java.util.HashMap;
import java.util.UUID;

public class AmogusRingRightClick implements Listener {
    private int taskID;

    private final RingCraftPlus main;
    public AmogusRingRightClick(RingCraftPlus main) {
        this.main = main;
    }

    private HashMap<UUID, Long> cooldowns = new HashMap<>();
    private long cooldownDuration = 1 * 1000; // Cooldown duration in milliseconds (e.g., 60 seconds)

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getAction().isRightClick() && event.getItem().getLore().equals(CustomItems.AmogusRing.getLore())){
            if (isOnCooldown(player)) {
                event.setCancelled(true);
                displayCooldownTime(player);
                return;
            }
            event.setCancelled(true);
            //setCooldown(player);
            LivingEntity entity = (LivingEntity) player.getTargetEntity(100);
            Location eLoc = entity.getLocation();
            Location pLoc = player.getEyeLocation();
            Particle particle = Particle.FLAME;
            if (player.getTargetEntity(100) != null && player.getTargetEntity(100) instanceof LivingEntity){
                particleLine(pLoc, eLoc, particle);
                Vector velocity = entity.getVelocity();
                velocity.setY(1.5);
                entity.setVelocity(velocity);
                setCooldown(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1, 1);
                entity.addPotionEffect(PotionEffectType.GLOWING.createEffect(20 * 4, 1));
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 1, 1);
                    entity.setGravity(false);
                    entity.setVelocity(velocity.setY(0));
                    entity.setAI(false);
                    spawnParticleCircle(entity, -0.5, 1);
                    spawnParticleCircle(entity, -1, 2);
                    spawnParticleCircle(entity, 1.5, 1);
                    spawnParticleCircle(entity, 2, 2);
                }, 15);
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    entity.setGravity(true);
                    entity.setAI(true);
                    spawnParticleCircle(entity, 1.5, 2);
                    spawnParticleCircle(entity, -0.5, 2);
                    entity.setVelocity(velocity.setY(-1));
                    player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1, 1);
                }, 20 * 4);
            }

        }
    }

    public static void particleLine(Location start, Location end, Particle particleType) {
        World world = start.getWorld();
        Vector direction = createLine(start, end).normalize();
        double distance = start.distance(end);
        for (double i = 0; i < distance; i += 1) {
            Vector offset = direction.clone().multiply(i);
            Location particleLoc = start.clone().add(offset);
            world.spawnParticle(particleType, particleLoc, 0);
        }
        world.spawnParticle(particleType, end, 0);
    }
    private void spawnParticleCircle(Entity player, double yOfset, double radius){
        Location loc = player.getLocation();
        loc.setY(loc.getY() + yOfset);
        for (double i = 0; i <360; i +=5){
            double angle = i * Math.PI / 180;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            loc.getWorld().spawnParticle(Particle.FLAME, loc.getX() + x, loc.getY() + 0.5, loc.getZ() + z, 0);
        }
    }
    public static Vector createLine(Location point1, Location point2) {
        double deltaX = point2.getX() - point1.getX();
        double deltaY = point2.getY() - point1.getY();
        double deltaZ = point2.getZ() - point1.getZ();
        return new Vector(deltaX, deltaY, deltaZ);
    }


    private boolean isOnCooldown(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            long currentTime = System.currentTimeMillis();
            long lastUseTime = cooldowns.get(player.getUniqueId());
            return (currentTime - lastUseTime) < cooldownDuration;
        }
        return false;
    }
    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void displayCooldownTime(Player player) {
        long currentTime = System.currentTimeMillis();
        long lastUseTime = cooldowns.get(player.getUniqueId());
        long remainingTimeMillis = cooldownDuration - (currentTime - lastUseTime);

        int remainingSeconds = (int) (remainingTimeMillis / 1000);
        player.sendMessage(Colorize.format("&7AMOGUS is on cooldown! Use again in: " + remainingSeconds + " seconds"));
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT,1, 1);
    }
}
