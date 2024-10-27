package dev.aurelium.auraskills.sponge.hooks;

import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.user.SpongeUser;
import dev.aurelium.auraskills.common.hooks.EconomyHook;
import dev.aurelium.auraskills.common.hooks.Hook;
import dev.aurelium.auraskills.common.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

public class VaultHook extends EconomyHook {

    @Nullable
    private final Economy economy;

    public VaultHook(AuraSkills plugin, ConfigurationNode config) {
        super(plugin, config);
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        } else {
            economy = null;
        }
    }

    @Override
    public void deposit(User user, double amount) {
        Player player = ((SpongeUser) user).getPlayer();
        if (player != null && economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    @Override
    public Class<? extends Hook> getTypeClass() {
        return EconomyHook.class;
    }
}
