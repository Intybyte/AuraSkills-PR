package dev.aurelium.auraskills.sponge.api.implementation;

import dev.aurelium.auraskills.api.loot.LootManager;
import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.common.api.implementation.ApiProvider;

public class SpongeApiProvider implements ApiProvider {

    private final LootManager lootManager;

    public SpongeApiProvider(AuraSkills plugin) {
        this.lootManager = new ApiLootManager(plugin);
    }

    @Override
    public LootManager getLootManager() {
        return lootManager;
    }
}
