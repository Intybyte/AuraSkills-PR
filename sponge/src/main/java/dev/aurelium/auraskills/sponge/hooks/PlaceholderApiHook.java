package dev.aurelium.auraskills.sponge.hooks;

import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.user.SpongeUser;
import dev.aurelium.auraskills.common.hooks.Hook;
import dev.aurelium.auraskills.common.hooks.PlaceholderHook;
import dev.aurelium.auraskills.common.user.User;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;

public class PlaceholderApiHook extends PlaceholderHook {

    public PlaceholderApiHook(AuraSkills plugin, ConfigurationNode config) {
        super(plugin, config);
        new PlaceholderApiProvider(plugin, "auraskills").register();
        new PlaceholderApiProvider(plugin, "aureliumskills").register();
    }

    @Override
    public String setPlaceholders(User user, String message) {
        Player player = ((SpongeUser) user).getPlayer();
        if (player != null) {
            return PlaceholderAPI.setPlaceholders(player, message);
        } else {
            return message;
        }
    }

    @Override
    public Class<? extends Hook> getTypeClass() {
        return PlaceholderHook.class;
    }
}