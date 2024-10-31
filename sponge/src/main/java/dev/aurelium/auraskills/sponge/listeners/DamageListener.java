package dev.aurelium.auraskills.sponge.listeners;

import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.damage.DamageHandler;
import dev.aurelium.auraskills.api.damage.DamageType;
import dev.aurelium.auraskills.sponge.damage.DamageResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.arrow.Arrow;
import org.spongepowered.api.entity.projectile.arrow.SpectralArrow;
import org.spongepowered.api.entity.projectile.arrow.Trident;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.tag.ItemTypeTags;

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
        Entity attackerEntity = event.cause().first(Entity.class).orElse(null);
        ServerPlayer player = getPlayerAttacker(attackerEntity);

        DamageSource dmgSource = event.cause().first(DamageSource.class).orElse(null);
        org.spongepowered.api.event.cause.entity.damage.DamageType damageType = null;
        if (dmgSource != null) {
            damageType = dmgSource.type();;
        }

        if (player != null) {
            if (plugin.getWorldManager().isInDisabledWorld(player.serverLocation())) {
                return;
            }
            //TODO: Figure out what this NPC metadata refers to or checks
            //if (player.hasMetadata("NPC")) return;
            if (damageType == DamageTypes.THORNS) return;
        }

        // Handles being damaged
        if (entity instanceof ServerPlayer target) {
            if (plugin.getWorldManager().isInDisabledWorld(target.serverLocation())) {
                return;
            }
            //if (target.hasMetadata("NPC")) return;
        }

        if (player == null && !(entity instanceof ServerPlayer)) {
            // We have nothing to do here
            return;
        }

        DamageResult result = damageHandler.handleDamage(
                player, entity, getDamageType(attackerEntity, player),
                damageType, event.finalDamage(), "vanilla");

        if (result.cancel()) {
            event.setCancelled(true);
        } else {
            event.setDamage(result.damage());
        }
    }

    @SuppressWarnings("deprecation")
    private DamageType getDamageType(Entity damager, ServerPlayer player) {
        if (player == null) return DamageType.OTHER;
        if (damager instanceof Arrow || damager instanceof SpectralArrow || damager instanceof Trident) {
            return DamageType.BOW;
        }
        ItemType material = player.itemInHand(HandTypes.MAIN_HAND).type();
        if (material.is(ItemTypeTags.SWORDS)) {
            return DamageType.SWORD;
        } else if (material.is(ItemTypeTags.AXES)) {
            return DamageType.AXE;
        } else if (material.is(ItemTypeTags.PICKAXES)) {
            return DamageType.PICKAXE;
        } else if (material.is(ItemTypeTags.SHOVELS)) {
            //this is the past checked for items named SPADES too
            return DamageType.SHOVEL;
        } else if (material.is(ItemTypeTags.HOES)) {
            return DamageType.HOE;
        } else if (material == ItemTypes.AIR) {
            return DamageType.HAND;
        }

        return DamageType.OTHER;
    }

    @Nullable
    private ServerPlayer getPlayerAttacker(Entity source) {
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
