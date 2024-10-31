package dev.aurelium.auraskills.api.damage;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.projectile.source.ProjectileSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DamageMeta {

    private final DamageType damageType;
    private final org.spongepowered.api.event.cause.entity.damage.DamageType damageCause;
    private final List<DamageModifier> attackModifiers = new ArrayList<>();
    private final List<DamageModifier> defenseModifiers = new ArrayList<>();
    private final Entity attacker;
    private final Entity target;
    private final String source;
    private double damage;

    public DamageMeta(@Nullable Entity attacker, Entity target, DamageType damageType, org.spongepowered.api.event.cause.entity.damage.DamageType damageCause, double damage, String source) {
        this.attacker = attacker;
        this.target = target;
        this.damageType = damageType;
        this.damageCause = damageCause;
        this.damage = damage;
        this.source = source;
    }

    public double getBaseDamage() {
        return damage;
    }

    public org.spongepowered.api.event.cause.entity.damage.DamageType getDamageType() {
        return damageCause;
    }

    public List<DamageModifier> getAttackModifiers() {
        return attackModifiers;
    }

    public void addAttackModifier(DamageModifier modifier) {
        this.attackModifiers.add(modifier);
    }

    public List<DamageModifier> getDefenseModifiers() {
        return defenseModifiers;
    }

    public void addDefenseModifier(DamageModifier modifier) {
        this.defenseModifiers.add(modifier);
    }

    public DamageType getAureliumDamageType() {
        return damageType;
    }

    @Nullable
    public Entity getAttacker() {
        return attacker;
    }

    public Entity getTarget() {
        return target;
    }

    @Nullable
    public ServerPlayer getAttackerAsPlayer() {
        if (attacker instanceof ServerPlayer) {
            return (ServerPlayer) attacker;
        }
        if (attacker instanceof Projectile) {
            Optional<Value.Mutable<ProjectileSource>> optionalShooter = ((Projectile) attacker).shooter();
            if(optionalShooter.isEmpty()) {
                return null;
            }

            ProjectileSource shooter = optionalShooter.get().get();
            if (optionalShooter.get().get() instanceof ServerPlayer) {
                return (ServerPlayer) shooter;
            }
        }
        return null;
    }

    @Nullable
    public ServerPlayer getTargetAsPlayer() {
        if (target instanceof ServerPlayer) {
            return (ServerPlayer) target;
        }
        return null;
    }

    @Nullable
    public SkillsUser getAttackerAsUser() {
        ServerPlayer player = getAttackerAsPlayer();
        if (player != null) {
            return AuraSkillsApi.get().getUser(player.uniqueId());
        }
        return null;
    }

    @Nullable
    public SkillsUser getTargetAsUser() {
        ServerPlayer player = getTargetAsPlayer();
        if (player != null) {
            return AuraSkillsApi.get().getUser(player.uniqueId());
        }
        return null;
    }

    public void clearAttackModifiers() {
        this.attackModifiers.clear();
    }

    public void clearDefenseModifiers() {
        this.defenseModifiers.clear();
    }

    public String getSource() {
        return source;
    }
}
