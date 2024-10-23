package dev.aurelium.auraskills.sponge.antiafk.checks;

import dev.aurelium.auraskills.api.event.skill.XpGainEvent;
import dev.aurelium.auraskills.api.source.type.DamageXpSource;
import dev.aurelium.auraskills.sponge.antiafk.AntiAfkManager;
import dev.aurelium.auraskills.sponge.antiafk.Check;
import dev.aurelium.auraskills.sponge.antiafk.CheckType;
import dev.aurelium.auraskills.sponge.antiafk.handler.FacingHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class DamageB extends Check {

    private final FacingHandler handler;

    public DamageB(CheckType type, AntiAfkManager manager) {
        super(type, manager);
        this.handler = new FacingHandler(optionInt("min_count"));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onXpGain(XpGainEvent event) {
        if (isDisabled() || !(event.getSource() instanceof DamageXpSource)) return;

        Player player = event.getPlayer();
        if (handler.failsCheck(getCheckData(player), player)) {
            event.setCancelled(true);
            logFail(player);
        }
    }

}
