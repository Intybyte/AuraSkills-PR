package dev.aurelium.auraskills.sponge.modifier;

import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.item.SkillsItem;
import dev.aurelium.auraskills.api.skill.Multiplier;
import dev.aurelium.auraskills.sponge.user.SpongeUser;
import dev.aurelium.auraskills.common.modifier.ModifierManager;
import dev.aurelium.auraskills.common.user.User;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;

import java.util.HashSet;
import java.util.Set;

public class SpongeModifierManager implements ModifierManager {

    private final AuraSkills plugin;

    public SpongeModifierManager(AuraSkills plugin) {
        this.plugin = plugin;
    }

    public void reloadPlayer(ServerPlayer player) {
        User user = plugin.getUser(player);
        Set<Stat> statsToReload = new HashSet<>();
        ItemStack item = player.itemInHand(HandTypes.MAIN_HAND);
        if (item.type() != ItemTypes.AIR) {
            SkillsItem skillsItem = new SkillsItem(item, plugin);
            for (StatModifier modifier : skillsItem.getStatModifiers(ModifierType.ITEM)) {
                user.removeStatModifier(modifier.name());
                statsToReload.add(modifier.stat());
            }
            for (Multiplier multiplier : skillsItem.getMultipliers(ModifierType.ITEM)) {
                user.removeMultiplier(multiplier.name());
            }
            if (skillsItem.meetsRequirements(ModifierType.ITEM, player)) {
                for (StatModifier modifier : skillsItem.getStatModifiers(ModifierType.ITEM)) {
                    user.addStatModifier(modifier, false);
                    statsToReload.add(modifier.stat());
                }
                for (Multiplier multiplier : skillsItem.getMultipliers(ModifierType.ITEM)) {
                    user.addMultiplier(multiplier);
                }
            }
        }

        ItemStack itemOffHand = player.itemInHand(HandTypes.OFF_HAND);
        if (!(itemOffHand.type() == ItemTypes.AIR)) {
            SkillsItem skillsItem = new SkillsItem(itemOffHand, plugin);
            for (StatModifier modifier : skillsItem.getStatModifiers(ModifierType.ITEM)) {
                user.removeStatModifier(modifier.name() + ".Offhand");
                statsToReload.add(modifier.stat());
            }
            for (Multiplier multiplier : skillsItem.getMultipliers(ModifierType.ITEM)) {
                user.removeMultiplier(multiplier.name() + ".Offhand");
            }
            if (skillsItem.meetsRequirements(ModifierType.ITEM, player)) {
                for (StatModifier modifier : skillsItem.getStatModifiers(ModifierType.ITEM)) {
                    StatModifier offHandModifier = new StatModifier(modifier.name() + ".Offhand", modifier.stat(), modifier.value());
                    user.addStatModifier(offHandModifier, false);
                    statsToReload.add(modifier.stat());
                }
                for (Multiplier multiplier : skillsItem.getMultipliers(ModifierType.ITEM)) {
                    Multiplier offHandMultiplier = new Multiplier(multiplier.name() + ".Offhand", multiplier.skill(), multiplier.value());
                    user.addMultiplier(offHandMultiplier);
                }
            }
        }

        EquipmentInventory equipment = player.inventory().armor();
        equipment.peek(EquipmentTypes.HEAD).ifPresent(it -> handleArmorPiece(it, player, user, statsToReload));
        equipment.peek(EquipmentTypes.CHEST).ifPresent(it -> handleArmorPiece(it, player, user, statsToReload));
        equipment.peek(EquipmentTypes.LEGS).ifPresent(it -> handleArmorPiece(it, player, user, statsToReload));
        equipment.peek(EquipmentTypes.FEET).ifPresent(it -> handleArmorPiece(it, player, user, statsToReload));

        for (Stat stat : statsToReload) {
            plugin.getStatManager().reloadStat(user, stat);
        }
    }

    private void handleArmorPiece(ItemStack armor, ServerPlayer player, User user, Set<Stat> statsToReload) {
        if (armor.type() == ItemTypes.AIR) {
            return;
        }

        SkillsItem skillsItem = new SkillsItem(armor, plugin);
        for (StatModifier modifier : skillsItem.getStatModifiers(ModifierType.ARMOR)) {
            user.removeStatModifier(modifier.name());
            statsToReload.add(modifier.stat());
        }
        for (Multiplier multiplier : skillsItem.getMultipliers(ModifierType.ARMOR)) {
            user.removeMultiplier(multiplier.name());
        }

        if (!skillsItem.meetsRequirements(ModifierType.ARMOR, player)) {
            return;
        }

        for (StatModifier modifier : skillsItem.getStatModifiers(ModifierType.ARMOR)) {
            user.addStatModifier(modifier, false);
            statsToReload.add(modifier.stat());
        }
        for (Multiplier multiplier : skillsItem.getMultipliers(ModifierType.ARMOR)) {
            user.addMultiplier(multiplier);
        }
    }

    @Override
    public void reloadUser(User user) {
        ServerPlayer player = ((SpongeUser) user).getPlayer();
        if (player != null) {
            reloadPlayer(player);
        }
    }
}
