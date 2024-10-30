package dev.aurelium.auraskills.api;

import org.jetbrains.annotations.ApiStatus;

public final class AuraSkillsSpongeProvider {

    private static AuraSkillsSponge instance = null;

    /**
     * Gets the instance of {@link AuraSkillsApi} containing API classes and methods.
     *
     * @return the API instance
     */
    public static AuraSkillsSponge getInstance() {
        AuraSkillsSponge instance = AuraSkillsSpongeProvider.instance;
        if (instance == null) {
            throw new IllegalStateException("AuraSkillsBukkit is not initialized");
        }
        return instance;
    }

    @ApiStatus.Internal
    static void register(AuraSkillsSponge instance) {
        AuraSkillsSpongeProvider.instance = instance;
    }

    @ApiStatus.Internal
    static void unregister() {
        AuraSkillsSpongeProvider.instance = null;
    }

    @ApiStatus.Internal
    private AuraSkillsSpongeProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }


}
