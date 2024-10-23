package dev.aurelium.auraskills.sponge.loot.type;

import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootValues;
import dev.aurelium.auraskills.sponge.loot.entity.EntitySupplier;

public class EntityLoot extends Loot {
    
    private final EntitySupplier entity;

    public EntityLoot(LootValues values, EntitySupplier entity) {
        super(values);
        this.entity = entity;
    }

    public EntitySupplier getEntity() {
        return entity;
    }
}
