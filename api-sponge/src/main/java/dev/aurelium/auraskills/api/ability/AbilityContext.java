package dev.aurelium.auraskills.api.ability;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsSponge;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class AbilityContext {

    private final AuraSkillsApi api;

    public AbilityContext(AuraSkillsApi api) {
        this.api = api;
    }

    /**
     * Gets whether the ability or it's parent skill is disabled
     *
     * @param ability the ability to check
     * @return whether the ability is disabled
     */
    public boolean isDisabled(Ability ability) {
        return !ability.isEnabled() || !ability.getSkill().isEnabled();
    }

    /**
     * Performs multiple checks to see whether the player should be allowed to use the ability.
     * Checks for the ability being locked, the location being disabled, inadequate permissions, among
     * other checks.
     *
     * @param player the player to check
     * @param ability the ability to check for
     * @return true if one of the checks failed, false if all checks passed
     */
    public boolean failsChecks(ServerPlayer player, Ability ability) {
        if (player == null) return true;
        if (!ability.isEnabled()) {
            return true;
        }
        if (api.getUser(player.uniqueId()).getAbilityLevel(ability) <= 0) {
            return true;
        }
        if (AuraSkillsSponge.get().getLocationManager().isPluginDisabled(player.serverLocation(), player)) {
            return true;
        }
        if (!api.getUser(player.uniqueId()).hasSkillPermission(ability.getSkill())) {
            return true;
        }
        if (api.getMainConfig().isDisabledInCreative()) {
            return player.gameMode().equals(GameModes.CREATIVE);
        }
        return false;
    }

}
