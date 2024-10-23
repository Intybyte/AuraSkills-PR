package dev.aurelium.auraskills.sponge.loot.entity;

import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.loot.parser.CustomEntityParser;
import org.spongepowered.configurate.ConfigurationNode;

public class VanillaEntityParser implements CustomEntityParser {

    private final AuraSkills plugin;

    public VanillaEntityParser(AuraSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public EntitySupplier getEntitySupplier(ConfigurationNode config) {
        return new VanillaEntitySupplier(EntityProperties.fromConfig(config, plugin));
    }

    @Override
    public boolean shouldUseParser(ConfigurationNode config) {
        String entity = config.node("entity").getString();

        if (entity == null) return false;

        // If it has a colon, it's a custom entity
        // But if it starts with minecraft:, it's a vanilla entity stated explicitly
        return !entity.contains(":") || entity.startsWith("minecraft:");
    }
}
