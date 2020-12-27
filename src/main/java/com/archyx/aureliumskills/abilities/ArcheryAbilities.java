package com.archyx.aureliumskills.abilities;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.skills.PlayerSkill;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.SkillLoader;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class ArcheryAbilities extends AbilityProvider implements Listener {

    private final Random r = new Random();

    public ArcheryAbilities(AureliumSkills plugin) {
        super(plugin, Skill.ARCHERY);
    }

    public void bowMaster(EntityDamageByEntityEvent event, Player player, PlayerSkill playerSkill) {
        if (OptionL.isEnabled(Skill.ARCHERY)) {
            if (plugin.getAbilityManager().isEnabled(Ability.BOW_MASTER)) {
                if (!player.hasPermission("aureliumskills.archery")) {
                    return;
                }
                if (playerSkill.getAbilityLevel(Ability.BOW_MASTER) > 0) {
                    double multiplier = 1 + (getValue(Ability.BOW_MASTER, playerSkill) / 100);
                    event.setDamage(event.getDamage() * multiplier);
                }
            }
        }
    }

    public void stun(PlayerSkill playerSkill, LivingEntity entity) {
        if (r.nextDouble() < (getValue(Ability.STUN, playerSkill) / 100)) {
            if (entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
                AttributeInstance speed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                if (speed != null) {
                    //Applies stun
                    double reducedSpeed = speed.getValue() * 0.2;
                    AttributeModifier modifier = new AttributeModifier("AureliumSkills-Stun", -1 * reducedSpeed, AttributeModifier.Operation.ADD_NUMBER);
                    speed.addModifier(modifier);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            AttributeInstance newSpeed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                            if (newSpeed != null) {
                                for (AttributeModifier attributeModifier : newSpeed.getModifiers()) {
                                    if (attributeModifier.getName().equals("AureliumSkills-Stun")) {
                                        newSpeed.removeModifier(attributeModifier);
                                    }
                                }
                            }
                        }
                    }.runTaskLater(plugin, 40L);
                }
            }
        }
    }

    @EventHandler
    public void removeStun(PlayerQuitEvent event) {
        //Removes stun on logout
        AttributeInstance speed = event.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speed != null) {
            for (AttributeModifier attributeModifier : speed.getModifiers()) {
                if (attributeModifier.getName().equals("AureliumSkills-Stun")) {
                    speed.removeModifier(attributeModifier);
                }
            }
        }
    }

    public void piercing(EntityDamageByEntityEvent event, PlayerSkill playerSkill, Player player, Arrow arrow) {
        if (r.nextDouble() < (getValue(Ability.PIERCING, playerSkill) / 100)) {
            arrow.setBounce(false);
            Vector velocity = arrow.getVelocity();
            Arrow newArrow = event.getEntity().getWorld().spawnArrow(arrow.getLocation(), velocity, (float) velocity.length(), 0.0f);
            newArrow.setShooter(player);
            newArrow.setKnockbackStrength(arrow.getKnockbackStrength());
            newArrow.setFireTicks(arrow.getFireTicks());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void archeryListener(EntityDamageByEntityEvent event) {
        if (OptionL.isEnabled(Skill.ARCHERY)) {
            if (!event.isCancelled()) {
                if (event.getDamager() instanceof Arrow) {
                    Arrow arrow = (Arrow) event.getDamager();
                    if (arrow.getShooter() instanceof Player) {
                        Player player = (Player) arrow.getShooter();
                        if (blockAbility(player)) return;
                        //Applies abilities
                        if (SkillLoader.playerSkills.containsKey(player.getUniqueId())) {
                            PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
                            AbilityManager options = plugin.getAbilityManager();
                            if (options.isEnabled(Ability.STUN)) {
                                if (event.getEntity() instanceof LivingEntity) {
                                    LivingEntity entity = (LivingEntity) event.getEntity();
                                    stun(playerSkill, entity);
                                }
                            }
                            if (options.isEnabled(Ability.PIERCING)) {
                                piercing(event, playerSkill, player, arrow);
                            }
                        }
                    }
                }
            }
        }
    }

}