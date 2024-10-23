package dev.aurelium.auraskills.sponge.loot.parser;

import org.spongepowered.configurate.ConfigurationNode;

public interface CustomParser {

    boolean shouldUseParser(ConfigurationNode config);

}
