package dev.aurelium.auraskills.sponge.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ExternalItemProvider {

    @Nullable
    ItemStack getItem(String id);

}
