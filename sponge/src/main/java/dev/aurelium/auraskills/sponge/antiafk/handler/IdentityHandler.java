package dev.aurelium.auraskills.sponge.antiafk.handler;

import dev.aurelium.auraskills.sponge.antiafk.CheckData;

public record IdentityHandler(int minCount, String idKey) {

    public boolean failsCheck(CheckData data, Object currentVal) {
        Object previous = data.getCache(idKey, Object.class, null);
        if (currentVal == null) return false;

        data.setCache(idKey, currentVal);

        if (previous == null) return false;

        if (previous.equals(currentVal)) {
            data.incrementCount();
        } else {
            data.resetCount();
        }

        return data.getCount() >= minCount;
    }

}
