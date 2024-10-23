package dev.aurelium.auraskills.sponge.loot.parser;

import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;

public interface CustomItemParser extends CustomParser {

    ItemStack parseCustomItem(ConfigurationNode config);

}
