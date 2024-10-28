package dev.aurelium.auraskills.sponge.ability;

import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.api.ability.AbilityContext;
import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.common.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AbilityImpl extends AbilityContext {

    protected final AuraSkills plugin;
    protected final Random rand = new Random();
    private final List<Ability> abilities = new ArrayList<>();

    public AbilityImpl(AuraSkills plugin, Ability... abilities) {
        super(plugin.getApi());
        this.plugin = plugin;
        this.abilities.addAll(Arrays.asList(abilities));
    }

    public List<Ability> getAbilities() {
        return abilities;
    }

    public String replaceDescPlaceholders(String input, Ability ability, User user) {
        return input;
    }

    protected double getValue(Ability ability, User user) {
        return ability.getValue(user.getAbilityLevel(ability));
    }

    protected double getSecondaryValue(Ability ability, User user) {
        return ability.getSecondaryValue(user.getAbilityLevel(ability));
    }
}