package dev.aurelium.auraskills.sponge.region;

import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.common.config.ConfigurateLoader;
import dev.aurelium.auraskills.common.region.WorldManager;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SpongeWorldManager implements WorldManager {

    private Set<String> blockedWorlds;
    private Set<String> disabledWorlds;
    private Set<String> blockedCheckBlockReplaceWorlds;
    private final AuraSkills plugin;

    public SpongeWorldManager(AuraSkills plugin) {
        this.plugin = plugin;
    }

    public void loadWorlds() {
        ConfigurateLoader loader = new ConfigurateLoader(plugin, TypeSerializerCollection.builder().build());
        try {
            ConfigurationNode config = loader.loadUserFile("config.yml");

            int blockedWorldsLoaded = 0;
            blockedWorlds = new HashSet<>();
            disabledWorlds = new HashSet<>();
            blockedCheckBlockReplaceWorlds = new HashSet<>();
            for (String blockedWorld : config.node("blocked_worlds").getList(String.class, new ArrayList<>())) {
                assert blockedWorld.contains(":") : "World must be full id with key:world";
                blockedWorlds.add(blockedWorld);
                blockedWorldsLoaded++;
            }
            for (String blockedWorld : config.node("disabled_worlds").getList(String.class, new ArrayList<>())) {
                assert blockedWorld.contains(":") : "World must be full id with key:world";
                disabledWorlds.add(blockedWorld);
                blockedWorldsLoaded++;
            }
            for (String blockedWorld : config.node("check_block_replace", "blocked_worlds").getList(String.class, new ArrayList<>())) {
                assert blockedWorld.contains(":") : "World must be full id with key:world";
                blockedCheckBlockReplaceWorlds.add(blockedWorld);
                blockedWorldsLoaded++;
            }
            plugin.logger().info("Loaded " + blockedWorldsLoaded + " blocked/disabled world" + (blockedWorldsLoaded != 1 ? "s" : ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isInBlockedWorld(ServerLocation location) {
        Optional<ServerWorld> worldOpt = location.worldIfAvailable();
        if (worldOpt.isEmpty()) {
            return false;
        }
        ServerWorld world = worldOpt.get();

        return disabledWorlds.contains(world.key().asString()) || blockedWorlds.contains(world.key().asString());
    }

    public boolean isInDisabledWorld(ServerLocation location) {
        Optional<ServerWorld> worldOpt = location.worldIfAvailable();
        if (worldOpt.isEmpty()) {
            return false;
        }
        ServerWorld world = worldOpt.get();
        return disabledWorlds.contains(world.key().asString());
    }

    public boolean isCheckReplaceDisabled(ServerLocation location) {
        Optional<ServerWorld> worldOpt = location.worldIfAvailable();
        if (worldOpt.isEmpty()) {
            return false;
        }
        ServerWorld world = worldOpt.get();
        return blockedCheckBlockReplaceWorlds.contains(world.key().asString());
    }

    @Override
    public boolean isBlockedWorld(String worldName) {
        return blockedWorlds.contains(worldName);
    }

    @Override
    public boolean isDisabledWorld(String worldName) {
        return disabledWorlds.contains(worldName);
    }
}
