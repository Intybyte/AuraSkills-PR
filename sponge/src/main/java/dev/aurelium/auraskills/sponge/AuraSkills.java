package dev.aurelium.auraskills.sponge;

import co.aikar.commands.SpongeCommandManager;
import com.google.inject.Inject;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.sponge.ability.BukkitAbilityManager;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.ability.AbilityManager;
import dev.aurelium.auraskills.common.ability.AbilityRegistry;
import dev.aurelium.auraskills.common.api.ApiAuraSkills;
import dev.aurelium.auraskills.common.api.ApiRegistrationUtil;
import dev.aurelium.auraskills.common.api.implementation.ApiProvider;
import dev.aurelium.auraskills.common.config.ConfigProvider;
import dev.aurelium.auraskills.common.config.preset.PresetManager;
import dev.aurelium.auraskills.common.event.EventHandler;
import dev.aurelium.auraskills.common.hooks.HookManager;
import dev.aurelium.auraskills.common.item.ItemRegistry;
import dev.aurelium.auraskills.common.leaderboard.LeaderboardManager;
import dev.aurelium.auraskills.common.level.LevelManager;
import dev.aurelium.auraskills.common.level.XpRequirements;
import dev.aurelium.auraskills.common.mana.ManaAbilityManager;
import dev.aurelium.auraskills.common.mana.ManaAbilityRegistry;
import dev.aurelium.auraskills.common.menu.MenuHelper;
import dev.aurelium.auraskills.common.message.MessageKey;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.common.message.PlatformLogger;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.migration.MigrationManager;
import dev.aurelium.auraskills.common.modifier.ModifierManager;
import dev.aurelium.auraskills.common.region.WorldManager;
import dev.aurelium.auraskills.common.reward.RewardManager;
import dev.aurelium.auraskills.common.scheduler.Scheduler;
import dev.aurelium.auraskills.common.skill.SkillManager;
import dev.aurelium.auraskills.common.skill.SkillRegistry;
import dev.aurelium.auraskills.common.source.SourceTypeRegistry;
import dev.aurelium.auraskills.common.stat.StatManager;
import dev.aurelium.auraskills.common.stat.StatRegistry;
import dev.aurelium.auraskills.common.storage.StorageProvider;
import dev.aurelium.auraskills.common.storage.backup.BackupProvider;
import dev.aurelium.auraskills.common.trait.TraitManager;
import dev.aurelium.auraskills.common.trait.TraitRegistry;
import dev.aurelium.auraskills.common.ui.UiProvider;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.user.UserManager;
import dev.aurelium.auraskills.common.util.PlatformUtil;
import dev.aurelium.auraskills.sponge.antiafk.AntiAfkManager;
import dev.aurelium.auraskills.sponge.api.implementation.BukkitApiProvider;
import dev.aurelium.auraskills.sponge.commands.CommandRegistrar;
import dev.aurelium.auraskills.sponge.commands.ConfirmManager;
import dev.aurelium.auraskills.sponge.config.BukkitConfigProvider;
import dev.aurelium.auraskills.sponge.event.BukkitEventHandler;
import dev.aurelium.auraskills.sponge.hooks.WorldGuardFlags;
import dev.aurelium.auraskills.sponge.item.ApiItemManager;
import dev.aurelium.auraskills.sponge.item.BukkitItemRegistry;
import dev.aurelium.auraskills.sponge.level.BukkitLevelManager;
import dev.aurelium.auraskills.sponge.logging.BukkitLogger;
import dev.aurelium.auraskills.sponge.loot.LootTableManager;
import dev.aurelium.auraskills.sponge.mana.BukkitManaAbilityManager;
import dev.aurelium.auraskills.sponge.menus.MenuFileManager;
import dev.aurelium.auraskills.sponge.message.BukkitMessageProvider;
import dev.aurelium.auraskills.sponge.modifier.BukkitModifierManager;
import dev.aurelium.auraskills.sponge.region.BukkitRegionManager;
import dev.aurelium.auraskills.sponge.region.BukkitWorldManager;
import dev.aurelium.auraskills.sponge.requirement.RequirementManager;
import dev.aurelium.auraskills.sponge.reward.BukkitRewardManager;
import dev.aurelium.auraskills.sponge.scheduler.BukkitScheduler;
import dev.aurelium.auraskills.sponge.stat.BukkitStatManager;
import dev.aurelium.auraskills.sponge.trait.BukkitTraitManager;
import dev.aurelium.auraskills.sponge.ui.BukkitUiProvider;
import dev.aurelium.auraskills.sponge.user.BukkitUserManager;
import dev.aurelium.auraskills.sponge.util.BukkitPlatformUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

/**
 * The main class of your Sponge plugin.
 *
 * <p>All methods are optional -- some common event registrations are included as a jumping-off point.</p>
 */
@Plugin("auraskills")
public class AuraSkills implements AuraSkillsPlugin {

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File dataFolder;

    private final PluginContainer container;
    private final Logger logger;

    public PluginContainer container() {
        return container;
    }

    private AuraSkillsApi api;
    private ApiProvider apiProvider;
    private SkillManager skillManager;
    private BukkitAbilityManager abilityManager;
    private BukkitManaAbilityManager manaAbilityManager;
    private StatManager statManager;
    private BukkitTraitManager traitManager;
    private SkillRegistry skillRegistry;
    private StatRegistry statRegistry;
    private TraitRegistry traitRegistry;
    private AbilityRegistry abilityRegistry;
    private ManaAbilityRegistry manaAbilityRegistry;
    private SourceTypeRegistry sourceTypeRegistry;
    private BukkitItemRegistry itemRegistry;
    private PlatformLogger platformLogger;
    private BukkitMessageProvider messageProvider;
    private BukkitConfigProvider configProvider;
    private BukkitLevelManager levelManager;
    private BukkitUserManager userManager;
    private XpRequirements xpRequirements;
    private HookManager hookManager;
    private WorldGuardFlags worldGuardFlags;
    private LeaderboardManager leaderboardManager;
    private BukkitUiProvider uiProvider;
    private RewardManager rewardManager;
    private Scheduler scheduler;
    private StorageProvider storageProvider;
    // private Slate slate; // needs new way to handle GUI... AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    private MenuFileManager menuFileManager;
    private SpongeCommandManager commandManager;

    private BukkitRegionManager regionManager;
    private BukkitWorldManager worldManager;
    private LootTableManager lootTableManager;
    private BukkitModifierManager modifierManager;
    private RequirementManager requirementManager;
    private BackupProvider backupProvider;
    private InventoryManager inventoryManager;
    private MenuHelper menuHelper;
    private EventHandler eventHandler;
    private ItemManager itemManager;
    private ConfirmManager confirmManager;
    private PresetManager presetManager;
    private PlatformUtil platformUtil;
    private AntiAfkManager antiAfkManager;
    private boolean nbtApiEnabled;

    @Inject
    AuraSkills(final PluginContainer container, final Logger logger) {
        this.container = container;
        this.logger = logger;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        this.logger.info("Constructing auraskills");
        
        // Register the API
        this.api = new ApiAuraSkills(this);
        this.apiProvider = new BukkitApiProvider(this);
        ApiRegistrationUtil.register(api);
        this.itemManager = new ApiItemManager(this); // Needed in ApiAuraSkillsBukkit

        platformLogger = new BukkitLogger(this);
        platformUtil = new BukkitPlatformUtil();
        // Load messages
        messageProvider = new BukkitMessageProvider(this);
        messageProvider.loadMessages();
        // Init managers
        skillManager = new SkillManager(this);
        abilityManager = new BukkitAbilityManager(this);
        manaAbilityManager = new BukkitManaAbilityManager(this);
        statManager = new BukkitStatManager(this);
        traitManager = new BukkitTraitManager(this);

        // Init registries
        skillRegistry = new SkillRegistry(this);
        statRegistry = new StatRegistry(this);
        traitRegistry = new TraitRegistry(this);
        abilityRegistry = new AbilityRegistry(this);
        manaAbilityRegistry = new ManaAbilityRegistry(this);
        sourceTypeRegistry = new SourceTypeRegistry();
        sourceTypeRegistry.registerDefaults();
        itemRegistry = new BukkitItemRegistry(this);
        itemRegistry.getStorage().load();
        // Create scheduler
        scheduler = new BukkitScheduler(this);
        eventHandler = new BukkitEventHandler(this);
        hookManager = new HookManager();
        userManager = new BukkitUserManager(this);
        presetManager = new PresetManager(this);

        generateConfigs(); // Generate default config files if missing
        generateDefaultMenuFiles();
        // Handle migration
        MigrationManager migrationManager = new MigrationManager(this);
        migrationManager.attemptConfigMigration();
        // Load config.yml file
        configProvider = new BukkitConfigProvider(this);
        configProvider.loadOptions(); // Also loads external plugin hooks
        initializeNbtApi();
        initializeMenus(); // Generate menu files
        // Initialize and migrate storage (connect to SQL database if enabled)
        initStorageProvider();
        migrationManager.attemptUserMigration();
        // Load blocked/disabled worlds lists
        worldManager = new BukkitWorldManager(this);
        worldManager.loadWorlds(); // Requires generateConfigs before
        regionManager = new BukkitRegionManager(this);
        backupProvider = new BackupProvider(this);
        xpRequirements = new XpRequirements(this);
        leaderboardManager = new LeaderboardManager(this);
        uiProvider = new BukkitUiProvider(this);
        modifierManager = new BukkitModifierManager(this);
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();
        rewardManager = new BukkitRewardManager(this); // Loaded later
        lootTableManager = new LootTableManager(this); // Loaded later
        confirmManager = new ConfirmManager(this);
        CommandRegistrar commandRegistrar = new CommandRegistrar(this);
        commandManager = commandRegistrar.registerCommands();
        messageProvider.setACFMessages(commandManager);
        levelManager = new BukkitLevelManager(this);
        antiAfkManager = new AntiAfkManager(this); // Requires config loaded
        registerPriorityEvents();
        // Enabled bStats
        Metrics metrics = new Metrics(container, logger, /*idk here*/, 21318);

        // Stuff to be run on the first tick
        scheduler.executeSync(() -> {
            loadSkills(); // Load skills, stats, abilities, etc from configs
            levelManager.registerLevelers(); // Requires skills loaded
            levelManager.loadXpRequirements(); // Requires skills loaded
            uiProvider.getBossBarManager().loadOptions(); // Requires skills registered
            requirementManager = new RequirementManager(this); // Requires skills registered
            rewardManager.loadRewards(); // Requires skills loaded
            lootTableManager.loadLootTables(); // Requires skills registered
            // Register default content
            traitManager.registerTraitImplementations();
            abilityManager.registerAbilityImplementations();
            manaAbilityManager.registerProviders();
            registerEvents();
            registerAndLoadMenus();
            // Call SkillsLoadEvent
            // SkillsLoadEvent event = new SkillsLoadEvent(skillManager.getSkillValues());
            // Bukkit.getPluginManager().callEvent(event);
            // Start updating leaderboards
            leaderboardManager.updateLeaderboards(); // Immediately update leaderboards
            leaderboardManager.startLeaderboardUpdater(); // 5 minute interval
            // bStats custom charts // ore and sponge are strict about this, better see it later
            // new MetricsUtil(getInstance()).registerCustomCharts(metrics);
        });
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        // Register a simple command
        // When possible, all commands should be registered within a command register event
        final Parameter.Value<String> nameParam = Parameter.string().key("name").build();
        event.register(this.container, Command.builder()
            .addParameter(nameParam)
            .permission("auraskills.command.greet")
            .executor(ctx -> {
                final String name = ctx.requireOne(nameParam);
                ctx.sendMessage(Identity.nil(), LinearComponents.linear(
                    NamedTextColor.AQUA,
                    Component.text("Hello "),
                    Component.text(name, Style.style(TextDecoration.BOLD)),
                    Component.text("!")
                ));

                return CommandResult.success();
            })
            .build(), "greet", "wave");
    }

    @Override
    public AuraSkillsApi getApi() {
        return api;
    }

    @Override
    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    @Override
    public ConfigProvider config() {
        return configProvider;
    }

    @Override
    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    @Override
    public ManaAbilityManager getManaAbilityManager() {
        return manaAbilityManager;
    }

    @Override
    public StatManager getStatManager() {
        return statManager;
    }

    @Override
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    @Override
    public LevelManager getLevelManager() {
        return levelManager;
    }

    @NotNull
    public User getUser(Player player) {
        return userManager.getUser(player);
    }

    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    @Override
    public XpRequirements getXpRequirements() {
        return xpRequirements;
    }

    @Override
    public PlatformLogger logger() {
        return platformLogger;
    }

    @Override
    public SkillManager getSkillManager() {
        return skillManager;
    }

    @Override
    public SkillRegistry getSkillRegistry() {
        return skillRegistry;
    }

    @Override
    public StatRegistry getStatRegistry() {
        return statRegistry;
    }

    @Override
    public TraitRegistry getTraitRegistry() {
        return traitRegistry;
    }

    @Override
    public TraitManager getTraitManager() {
        return traitManager;
    }

    @Override
    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    @Override
    public ManaAbilityRegistry getManaAbilityRegistry() {
        return manaAbilityRegistry;
    }

    @Override
    public SourceTypeRegistry getSourceTypeRegistry() {
        return sourceTypeRegistry;
    }

    @Override
    public HookManager getHookManager() {
        return hookManager;
    }

    @Override
    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    @Override
    public UiProvider getUiProvider() {
        return uiProvider;
    }

    @Override
    public RewardManager getRewardManager() {
        return rewardManager;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    @Override
    public BackupProvider getBackupProvider() {
        return backupProvider;
    }

    @Override
    public WorldManager getWorldManager() {
        return worldManager;
    }

    @Override
    public MenuHelper getMenuHelper() {
        return menuHelper;
    }

    @Override
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    @Override
    public PresetManager getPresetManager() {
        return presetManager;
    }

    @Override
    public PlatformUtil getPlatformUtil() {
        return platformUtil;
    }

    @Override
    public ApiProvider getApiProvider() {
        return apiProvider;
    }

    @Override
    public ModifierManager getModifierManager() {
        return modifierManager;
    }

    @Override
    public String getMsg(MessageKey key, Locale locale) {
        return messageProvider.get(key, locale);
    }

    @Override
    public String getPrefix(Locale locale) {
        return messageProvider.get(CommandMessage.PREFIX, locale);
    }

    public <T extends Audience> Locale getLocale(T sender) {
        if (sender instanceof Player player) {
            return getUser(player).getLocale();
        } else {
            return messageProvider.getDefaultLanguage();
        }
    }

    @Override
    public void runConsoleCommand(String command) {
        var server = Sponge.server();
        try {
            server.commandManager().process(Sponge.systemSubject(), command);
        } catch (Exception ignored) {}
    }

    @Override
    public void runPlayerCommand(User user, String command) {

    }

    @Override
    public InputStream getResource(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            URL url = this.getClass().getClassLoader().getResource(path);

            if (url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public void saveResource(String path, boolean replace) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        path = path.replace('\\', '/');
        InputStream in = getResource(path);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + path + "' cannot be found");
        }

        File outFile = new File(dataFolder, path);
        int lastIndex = path.lastIndexOf('/');
        File outDir = new File(dataFolder, path.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                logger.warn("Could not save {} to {} because {} already exists.", outFile.getName(), outFile, outFile.getName());
            }
        } catch (IOException ex) {
            logger.error("Could not save {} to {}", outFile.getName(), outFile, ex);
        }
    }

    @Override
    public File getPluginFolder() {
        return dataFolder;
    }
}
