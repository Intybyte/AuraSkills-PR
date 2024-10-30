package dev.aurelium.auraskills.api.region;

import dev.aurelium.auraskills.api.skill.Skill;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;

public interface LocationManager {

    /**
     * Checks whether the given locations is blocked from giving XP by the config or WorldGuard.
     *
     * @param location the location to check
     * @return whether XP gain is blocked
     */
    boolean isXpGainBlocked(ServerLocation location, ServerPlayer player, Skill skill);

    /**
     * Gets whether the plugin features are disabled in a location. If disabled,
     * all skills, stats, and abilities should not function.
     *
     * @param location the location to check
     * @return whether the plugin is disabled
     */
    boolean isPluginDisabled(ServerLocation location, ServerPlayer player);

    /**
     * Gets whether tracking for player-placed blocks is disabled at a given location.
     *
     * @param location the location to check
     * @return whether check replace is disabled
     */
    boolean isCheckReplaceDisabled(ServerLocation location);

}
