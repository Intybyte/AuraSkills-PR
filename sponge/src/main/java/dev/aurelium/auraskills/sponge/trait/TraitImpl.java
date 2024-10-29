package dev.aurelium.auraskills.sponge.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.api.bukkit.SpongeTraitHandler;
import org.bukkit.event.Listener;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public abstract class TraitImpl implements SpongeTraitHandler, Listener {

    protected final AuraSkills plugin;
    private final Trait[] traits;

    TraitImpl(AuraSkills plugin, Trait... traits) {
        this.plugin = plugin;
        this.traits = traits;
    }

    @Override
    public Trait[] getTraits() {
        return traits;
    }

    @Override
    public void onReload(ServerPlayer player, SkillsUser user, Trait trait) {
        reload(player, trait);
    }

    protected void reload(ServerPlayer player, Trait trait) {

    }

}
