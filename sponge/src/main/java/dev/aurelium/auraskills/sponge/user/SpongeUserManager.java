package dev.aurelium.auraskills.sponge.user;

import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.common.skill.LoadedSkill;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.user.UserManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpongeUserManager implements UserManager {

    private final AuraSkills plugin;
    private final Map<UUID, User> playerDataMap = new ConcurrentHashMap<>();

    public SpongeUserManager(AuraSkills plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public User getUser(org.spongepowered.api.entity.living.player.User player) {
        User user = playerDataMap.get(player.uniqueId());
        if (user != null) {
            return user;
        } else {
            return createNewUser(player.uniqueId());
        }
    }

    public User getUser(ServerPlayer player) {
        return getUser(player.user());
    }

    @Override
    @Nullable
    public User getUser(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    @Override
    public void addUser(User user) {
        playerDataMap.put(user.getUuid(), user);
    }

    @Override
    public void removeUser(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    @Override
    public boolean hasUser(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }

    @Override
    public Map<UUID, User> getUserMap() {
        return playerDataMap;
    }

    @Override
    public User createNewUser(UUID uuid) {
        Optional<ServerPlayer> player = Sponge.server().player(uuid);

        User user;
        if (player.isEmpty()) {
            user = new SpongeUser(uuid, null, plugin);
        } else {
            user = new SpongeUser(uuid, player.get().user(), plugin);
        }
        // Set all skills to level 1 for new players
        for (LoadedSkill loadedSkill : plugin.getSkillManager().getSkills()) {
            user.setSkillLevel(loadedSkill.skill(), plugin.config().getStartLevel());
            user.setSkillXp(loadedSkill.skill(), 0.0);
        }
        return user;
    }

    @Override
    public List<User> getOnlineUsers() {
        List<User> online = new ArrayList<>();
        for (ServerPlayer player : Sponge.server().onlinePlayers()) {
            User user = playerDataMap.get(player.uniqueId());
            if (user != null) {
                online.add(user);
            }
        }
        return online;
    }
}
