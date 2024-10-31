package dev.aurelium.auraskills.sponge.listeners;

import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.damage.DamageHandler;
import dev.aurelium.auraskills.api.damage.DamageType;
import dev.aurelium.auraskills.sponge.damage.DamageResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.projectile.source.ProjectileSource;

import java.util.Optional;

public class DamageListener {

    private final AuraSkills plugin;
    private final DamageHandler damageHandler;

    public DamageListener(AuraSkills plugin) {
        this.plugin = plugin;
        this.damageHandler = new DamageHandler();
    }

    @Listener
    public void onDamage(final DamageEntityEvent event) {
        // Check if not cancelled
        if (event.isCancelled()) {
            return;
        }

        // Get the entity that is damaged
        Entity entity = event.entity();

        // Gets the player who dealt damage
        ServerPlayer player = getDamager(event.cause());

        if (player != null) {
            if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
                return;
            }
            if (player.hasMetadata("NPC")) return;
            if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return;
        }

        // Handles being damaged
        if (event.getEntity() instanceof Player target) {
            if (plugin.getWorldManager().isInDisabledWorld(target.getLocation())) {
                return;
            }
            if (target.hasMetadata("NPC")) return;
        }

        if (player == null && !(event.getEntity() instanceof Player)) {
            // We have nothing to do here
            return;
        }

        DamageResult result = damageHandler.handleDamage(
                event.getDamager(), event.getEntity(), getDamageType(event, player),
                event.getCause(), event.getDamage(), "vanilla");

        if (result.cancel()) {
            event.setCancelled(true);
        } else {
            event.setDamage(result.damage());
        }
    }

    @SuppressWarnings("deprecation")
    private DamageType getDamageType(EntityDamageByEntityEvent event, Player player) {
        if (player == null) return DamageType.OTHER;
        if (event.getDamager() instanceof Arrow || event.getDamager() instanceof SpectralArrow || event.getDamager() instanceof TippedArrow) {
            return DamageType.BOW;
        }
        Material material = player.getInventory().getItemInMainHand().getType();
        if (material.name().contains("SWORD")) {
            return DamageType.SWORD;
        } else if (material.name().contains("_AXE")) {
            return DamageType.AXE;
        } else if (material.name().contains("PICKAXE")) {
            return DamageType.PICKAXE;
        } else if (material.name().contains("SHOVEL") || material.name().contains("SPADE")) {
            return DamageType.SHOVEL;
        } else if (material.name().contains("HOE")) {
            return DamageType.HOE;
        } else if (material.equals(Material.AIR)) {
            return DamageType.HAND;
        } else if (event.getDamager() instanceof Trident) {
            return DamageType.BOW;
        }
        return DamageType.OTHER;
    }

    @Nullable
    private ServerPlayer getDamager(Cause cause) {
        Entity source = cause.first(Entity.class).orElse(null);
        if (source == null) {
            return null;
        }

        ServerPlayer player = null;
        if (source instanceof ServerPlayer) {
            player = (ServerPlayer) source;
        } else if (source instanceof Projectile projectile) {
            EntityType<?> type = projectile.type();
            if (type == EntityTypes.ARROW || type == EntityTypes.SPECTRAL_ARROW || type == EntityTypes.TRIDENT /*
                There was tipped arrow too, but can't find it and it is probably inside EntityTypes.ARROW,
                idc about compatibility for now as it is a TODO backwards compatibility for entityTypes
                it is more important to get this up and running, the api differences between versions looks major in some part
                so no idea if i will be able to add compatibility
            */) {
                Optional<Value.Mutable<ProjectileSource>> optionalShooter = projectile.shooter();

                if (optionalShooter.isEmpty()) return null;


                if (optionalShooter.get() instanceof ServerPlayer) {
                    player = (ServerPlayer) optionalShooter.get();
                }
            }
        }
        return player;
    }
}
