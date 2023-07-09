package dev.aurelium.auraskills.bukkit.leveler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.common.player.User;
import dev.aurelium.auraskills.common.util.data.Pair;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BlockLeveler implements Listener {

    private final AuraSkills plugin;

    public BlockLeveler(AuraSkills plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        User user = plugin.getUser(player);

        Pair<BlockXpSource, Skill> sourcePair = identifySource(player, user, event, BlockXpSource.BlockTriggers.BREAK);
        if (sourcePair == null) {
            return;
        }

        BlockXpSource source = sourcePair.getFirst();
        Skill skill = sourcePair.getSecond();

        if (skill.optionBoolean("check_cancelled", true) && event.isCancelled()) {
            return;
        }

        if (!player.hasPermission("auraskills.skill." + skill.name())) {
            return;
        }

        plugin.getLeveler().addXp(user, skill, source.getXp());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

    }

    @Nullable
    private Pair<BlockXpSource, Skill> identifySource(Player player, User user, BlockBreakEvent event, BlockXpSource.BlockTriggers trigger) {
        Map<BlockXpSource, Skill> sources = plugin.getSkillManager().getSourcesOfType(BlockXpSource.class);
        sources = filterByTrigger(sources, trigger);
        for (Map.Entry<BlockXpSource, Skill> entry : sources.entrySet()) {
            BlockXpSource source = entry.getKey();
            Skill skill = entry.getValue();

            // Check block type (material)
            boolean blockMatches = false;
            for (String blockName : source.getBlocks()) {
                if (event.getBlock().getType().name().equalsIgnoreCase(blockName)) {
                    blockMatches = true;
                    break;
                }
            }
            if (!blockMatches) {
                continue;
            }

            Block block = event.getBlock(); // The block that matches the block type

            // Check block state
            boolean anyStateMatches = true;
            if (source.getStates() != null) {
                anyStateMatches = false;
                // Convert block data to json
                String blockDataString = block.getBlockData().getAsString(true);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(blockDataString, JsonObject.class);
                // Check if block data matches defined states
                for (BlockXpSource.BlockXpSourceState state : source.getStates()) {
                    if (state == null) continue;
                    boolean stateMatches = true;
                    for (Map.Entry<String, Object> stateEntry : state.getStateMap().entrySet()) {
                        String key = stateEntry.getKey();
                        Object value = stateEntry.getValue();
                        if (!jsonObject.has(key)) {
                            stateMatches = false;
                            break;
                        }
                        if (!jsonObject.get(key).getAsString().equals(String.valueOf(value))) {
                            stateMatches = false;
                            break;
                        }
                    }
                    // If one state matches, then the block matches and we can stop checking
                    if (stateMatches) {
                        anyStateMatches = true;
                        break;
                    }
                }
            }
            // Skip if no state matches
            if (!anyStateMatches) {
                continue;
            }

            return new Pair<>(source, skill);
        }
        return null;
    }

    private Map<BlockXpSource, Skill> filterByTrigger(Map<BlockXpSource, Skill> sources, BlockXpSource.BlockTriggers trigger) {
        Map<BlockXpSource, Skill> filtered = new HashMap<>();
        for (Map.Entry<BlockXpSource, Skill> entry : sources.entrySet()) {
            BlockXpSource source = entry.getKey();
            Skill skill = entry.getValue();
            // Check if trigger matches any of the source triggers
            for (BlockXpSource.BlockTriggers sourceTrigger : source.getTriggers()) {
                if (sourceTrigger == trigger) {
                    filtered.put(source, skill);
                    break;
                }
            }
        }
        return filtered;
    }

}
