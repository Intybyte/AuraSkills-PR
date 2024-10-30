package dev.aurelium.auraskills.sponge.stat;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.sponge.trait.SpongeTraitManager;
import dev.aurelium.auraskills.sponge.user.SpongeUser;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.stat.StatManager;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.api.sponge.SpongeTraitHandler;
import org.bukkit.entity.Player;

public class BukkitStatManager extends StatManager {

    public BukkitStatManager(AuraSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void reloadPlayer(User user) {
        // Reload traits
        Player player = ((SpongeUser) user).getPlayer();
        if (player == null) return;

        for (Trait trait : plugin.getTraitManager().getEnabledTraits()) {
            SpongeTraitHandler traitImpl = ((SpongeTraitManager) plugin.getTraitManager()).getTraitImpl(trait);
            if (traitImpl == null) continue;

            traitImpl.onReload(player, user.toApi(), trait);
        }
    }

    @Override
    public <T> void reload(User user, T type) {
        if (type instanceof Stat stat) {
            reloadStat(user, stat);
        } else if (type instanceof Trait trait) {
            for (Stat stat : plugin.getTraitManager().getLinkedStats(trait)) {
                reloadStat(user, stat);
            }
        }
    }

    @Override
    public void reloadStat(User user, Stat stat) {
        if (!stat.isEnabled()) return;
        Player player = ((SpongeUser) user).getPlayer();
        if (player == null) return;
        // Reload traits
        for (Trait trait : stat.getTraits()) {
            SpongeTraitHandler traitImpl = ((SpongeTraitManager) plugin.getTraitManager()).getTraitImpl(trait);
            if (traitImpl == null) continue;

            traitImpl.onReload(player, user.toApi(), trait);
        }
    }
}
