package dev.aurelium.auraskills.sponge.ability;

import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.skills.agility.AgilityAbilities;
import dev.aurelium.auraskills.sponge.skills.alchemy.AlchemyAbilities;
import dev.aurelium.auraskills.sponge.skills.archery.ArcheryAbilities;
import dev.aurelium.auraskills.sponge.skills.defense.DefenseAbilities;
import dev.aurelium.auraskills.sponge.skills.enchanting.EnchantingAbilities;
import dev.aurelium.auraskills.sponge.skills.endurance.EnduranceAbilities;
import dev.aurelium.auraskills.sponge.skills.excavation.ExcavationAbilities;
import dev.aurelium.auraskills.sponge.skills.farming.FarmingAbilities;
import dev.aurelium.auraskills.sponge.skills.fighting.FightingAbilities;
import dev.aurelium.auraskills.sponge.skills.fishing.FishingAbilities;
import dev.aurelium.auraskills.sponge.skills.foraging.ForagingAbilities;
import dev.aurelium.auraskills.sponge.skills.forging.ForgingAbilities;
import dev.aurelium.auraskills.sponge.skills.healing.HealingAbilities;
import dev.aurelium.auraskills.sponge.skills.mining.MiningAbilities;
import dev.aurelium.auraskills.common.ability.AbilityManager;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.user.User;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashMap;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;

public class SpongeAbilityManager extends AbilityManager {

    private final AuraSkills plugin;
    private final Map<Class<?>, AbilityImpl> abilityImpls = new HashMap<>();

    public SpongeAbilityManager(AuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void registerAbilityImplementations() {
        registerAbilityImpl(new FishingAbilities(plugin));
        registerAbilityImpl(new EnduranceAbilities(plugin));
        registerAbilityImpl(new AgilityAbilities(plugin));
        registerAbilityImpl(new AlchemyAbilities(plugin));
        registerAbilityImpl(new EnchantingAbilities(plugin));
        registerAbilityImpl(new HealingAbilities(plugin));
        registerAbilityImpl(new ForgingAbilities(plugin));
        registerAbilityImpl(new FightingAbilities(plugin));
        registerAbilityImpl(new ArcheryAbilities(plugin));
        registerAbilityImpl(new FarmingAbilities(plugin));
        registerAbilityImpl(new ForagingAbilities(plugin));
        registerAbilityImpl(new MiningAbilities(plugin));
        registerAbilityImpl(new ExcavationAbilities(plugin));
        registerAbilityImpl(new DefenseAbilities(plugin));
    }

    public void registerAbilityImpl(AbilityImpl abilityImpl) {
        abilityImpls.put(abilityImpl.getClass(), abilityImpl);
        Sponge.eventManager().registerListeners(plugin.container(), abilityImpl);
    }

    public <T extends AbilityImpl> T getAbilityImpl(Class<T> clazz) {
        AbilityImpl abilityImpl = abilityImpls.get(clazz);
        if (abilityImpl != null) {
            return clazz.cast(abilityImpl);
        }
        throw new IllegalArgumentException("Ability implementation of type " + clazz.getSimpleName() + " not found!");
    }

    @Nullable
    public AbilityImpl getAbilityImpl(Ability ability) {
        for (AbilityImpl impl : abilityImpls.values()) {
            if (impl.getAbilities().contains(ability)) {
                return impl;
            }
        }
        return null;
    }

    public void sendMessage(Player player, String message) {
        User user = plugin.getUser(player);
        if (plugin.configBoolean(Option.ACTION_BAR_ABILITY) && plugin.configBoolean(Option.ACTION_BAR_ENABLED)) {
            plugin.getUiProvider().getActionBarManager().sendAbilityActionBar(user, message);
        } else {
            if (message == null || message.isEmpty()) return; // Don't send empty message
            player.sendMessage(text(plugin.getPrefix(user.getLocale()) + message));
        }
    }

    @Override
    public String getBaseDescription(Ability ability, User user, boolean formatted) {
        String desc = ability.getDescription(user.getLocale(), formatted);
        AbilityImpl impl = plugin.getAbilityManager().getAbilityImpl(ability);
        if (impl != null) {
            desc = impl.replaceDescPlaceholders(desc, ability, user);
        }
        return desc;
    }

}