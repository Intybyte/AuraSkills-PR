package dev.aurelium.auraskills.api.event.damage;

import dev.aurelium.auraskills.api.damage.DamageMeta;
import dev.aurelium.auraskills.api.damage.DamageModifier;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Event;

public class DamageEvent implements Cancellable, Event {
    private final DamageMeta damageMeta;

    private boolean cancelled = false;

    public DamageEvent(DamageMeta damageMeta) {
        this.damageMeta = damageMeta;
    }

    public DamageMeta getDamageMeta() {
        return damageMeta;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public double getModifiedAttackDamage() {
        DamageCalculation calc = new DamageCalculation(damageMeta.getBaseDamage());

        double additive = 0.0;

        for (DamageModifier modifier : damageMeta.getAttackModifiers()) {
            additive += applyModifier(calc, modifier);
        }

        return calc.getDamage() * (1 + additive);
    }

    public double getModifiedDamage() {
        DamageCalculation calc = new DamageCalculation(damageMeta.getBaseDamage());

        double additive = 0.0;

        for (DamageModifier modifier : damageMeta.getAttackModifiers()) {
            additive += applyModifier(calc, modifier);
        }

        for (DamageModifier modifier : damageMeta.getDefenseModifiers()) {
            additive += applyModifier(calc, modifier);
        }

        return calc.getDamage() * (1 + additive);
    }

    @Override
    public Cause cause() {
        return damageMeta.getAttacker();
    }

    static class DamageCalculation {
        private double damage;

        public DamageCalculation(double baseDamage) {
            this.damage = baseDamage;
        }

        private void setDamage(double damage) {
            this.damage = damage;
        }

        public double getDamage() {
            return damage;
        }
    }

    private double applyModifier(DamageCalculation calculation, DamageModifier modifier) {
        switch (modifier.operation()) {
            case MULTIPLY:
                double multiplier = 1.0 + modifier.value();
                calculation.setDamage(calculation.getDamage() * multiplier);
                break;
            case ADD_BASE:
                calculation.setDamage(calculation.getDamage() + modifier.value());
                break;
            case ADD_COMBINED:
                return modifier.value();
        }
        return 0.0;
    }
}
