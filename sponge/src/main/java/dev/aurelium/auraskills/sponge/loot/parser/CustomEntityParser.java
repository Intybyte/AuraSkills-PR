package dev.aurelium.auraskills.sponge.loot.parser;

import dev.aurelium.auraskills.sponge.loot.entity.EntitySupplier;
import org.spongepowered.configurate.ConfigurationNode;

public interface CustomEntityParser extends CustomParser {

    EntitySupplier getEntitySupplier(ConfigurationNode config);

}
