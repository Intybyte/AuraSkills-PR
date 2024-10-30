package dev.aurelium.auraskills.api.sponge;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitHandler;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public interface SpongeTraitHandler extends TraitHandler {

    double getBaseLevel(ServerPlayer player, Trait trait);

    void onReload(ServerPlayer player, SkillsUser user, Trait trait);

}
