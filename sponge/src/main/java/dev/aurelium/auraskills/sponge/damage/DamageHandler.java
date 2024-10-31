package dev.aurelium.auraskills.sponge.damage;

import dev.aurelium.auraskills.api.damage.DamageMeta;
import dev.aurelium.auraskills.api.damage.DamageType;
import dev.aurelium.auraskills.api.event.damage.DamageEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;

public class DamageHandler {

    public DamageResult handleDamage(@Nullable Entity attacker, Entity target, DamageType damageType, org.spongepowered.api.event.cause.entity.damage.DamageType damageCause, double damage, String source) {
        var damageMeta = new DamageMeta(attacker, target, damageType, damageCause, damage, source);

        var event = new DamageEvent(damageMeta);
        Sponge.eventManager().post(event);

        if (event.isCancelled()) {
            return new DamageResult(damage, true);
        }

        double finalDamage = event.getModifiedDamage();

        return new DamageResult(finalDamage, false);
    }
}
