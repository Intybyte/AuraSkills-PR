package dev.aurelium.auraskills.sponge.hooks;

import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.hooks.Hook;
import dev.aurelium.auraskills.common.hooks.LuckPermsHook;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.spongepowered.api.Sponge;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SpongeLuckPermsHook extends LuckPermsHook {

    private final String prefix = "auraskills.multiplier.";
    private final Map<UUID, Set<String>> permissionCache = new ConcurrentHashMap<>();
    private final boolean usePermissionCache;

    public SpongeLuckPermsHook(AuraSkillsPlugin plugin, ConfigurationNode config) {
        super(plugin, config);

        this.usePermissionCache = config.node("use_permission_cache").getBoolean(true);

        if (!this.usePermissionCache) return;

        luckPerms.getEventBus().subscribe(NodeAddEvent.class,
                event -> handleEvent(event.getNode(), event.getTarget()));

        luckPerms.getEventBus().subscribe(NodeRemoveEvent.class,
                event -> handleEvent(event.getNode(), event.getTarget()));
    }

    public boolean usePermissionCache() {
        return usePermissionCache;
    }

    private void handleEvent(Node node, PermissionHolder target) {
        if (!(node instanceof PermissionNode) && !(node instanceof InheritanceNode)) return;

        if (node instanceof PermissionNode permissionNode) {
            if (!permissionNode.getValue()) return;
            if (!permissionNode.getPermission().startsWith(prefix)) return;
        }

        if (target instanceof User user) {
            plugin.getScheduler().scheduleAsync(
                    () -> {
                            var uuid = user.getUniqueId();
                            if (checkUUID(uuid)) {
                                permissionCache.put(uuid, getMultiplierPermissions(uuid));
                            }
                        },
                    500,
                    TimeUnit.MILLISECONDS
            );
        } else if (target instanceof Group group) {
            final List<UUID> affectedPlayers = new ArrayList<>(permissionCache.keySet().size());

            if (node instanceof InheritanceNode) {
                // This shouldn't really happen on a prod server too often.
                affectedPlayers.addAll(permissionCache.keySet());
            } else {
                permissionCache.keySet().forEach((key) -> {
                    User user = luckPerms.getUserManager().getUser(key);
                    if (user == null) return;

                    if (user.getInheritedGroups(QueryOptions.builder(QueryMode.CONTEXTUAL)
                                    .context(QueryOptions.defaultContextualOptions().context())
                                    .flag(Flag.RESOLVE_INHERITANCE, true).build())
                            .stream().anyMatch((g) -> group.getName().equals(g.getName()))) {
                        affectedPlayers.add(user.getUniqueId());
                    }
                });
            }

            plugin.getScheduler().scheduleAsync(() -> {
                for (UUID uuid : affectedPlayers) {
                    if (checkUUID(uuid)) {
                        permissionCache.put(uuid, getMultiplierPermissions(uuid));
                    }
                    permissionCache.put(uuid, getMultiplierPermissions(uuid));
                }
            }, 500, TimeUnit.MILLISECONDS);
        }
    }

    public Set<String> getMultiplierPermissions(org.spongepowered.api.entity.living.player.User player) {
        return permissionCache.computeIfAbsent(player.uniqueId(), this::getMultiplierPermissions);
    }

    private Set<String> getMultiplierPermissions(UUID uuid) {
        var userOptional = getUser(uuid);
        if (userOptional.isEmpty()) {
            return new HashSet<>();
        }

        var user = userOptional.get();

        var context = Sponge.server().serviceProvider().contextService().contextsFor(user.contextCause());
        var currentPermissions = user.transientSubjectData().permissions(context);
        return currentPermissions.entrySet()
                .stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .filter(p -> p.startsWith(prefix))
                .collect(Collectors.toSet());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Put it in the cache async
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.getScheduler().executeAsync(() -> {
            if (!event.getPlayer().isOnline()) return;
            permissionCache.put(uuid, getMultiplierPermissions(uuid));
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        permissionCache.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public Class<? extends Hook> getTypeClass() {
        return SpongeLuckPermsHook.class;
    }

    private Optional<org.spongepowered.api.entity.living.player.User> getUser(UUID uuid) {
        var userFuture = Sponge.server().userManager().load(uuid);
        try {
            return userFuture.get();
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private boolean checkUUID(UUID uuid) {
        var optionalUser = getUser(uuid);
        if (optionalUser.isEmpty()) {
            return false;
        }

        var user = optionalUser.get();
        return user.isOnline();
    }
}
