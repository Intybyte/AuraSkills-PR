package dev.aurelium.auraskills.sponge.menus.contexts;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.slate.context.ContextProvider;
import org.jetbrains.annotations.Nullable;

public class SkillContext implements ContextProvider<Skill> {

    private final AuraSkills plugin;

    public SkillContext(AuraSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<Skill> getType() {
        return Skill.class;
    }

    @Nullable
    @Override
    public Skill parse(String menuName, String input) {
        Skill skill = plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(input));
        if (skill != null && skill.isEnabled()) {
            return skill;
        } else {
            return null;
        }
    }
}
