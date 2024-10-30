package dev.aurelium.auraskills.sponge.region;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.sponge.AuraSkills;
import dev.aurelium.auraskills.sponge.source.BlockLeveler;
import dev.aurelium.auraskills.common.region.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;

public class SpongeRegionManager extends RegionManager {

    private final AuraSkills plugin;
    @Nullable
    private BlockLeveler blockLeveler; // Lazy initialized in handleBlockPlace

    public SpongeRegionManager(AuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public boolean isPlacedBlock(ServerLocation location) {
        int chunkX = location.chunkPosition().x();
        int chunkZ = location.chunkPosition().z();

        int regionX = (int) Math.floor((double) chunkX / 32.0);
        int regionZ = (int) Math.floor((double) chunkZ / 32.0);

        Region region = regions.get(new RegionCoordinate(location.world().key().asString(), regionX, regionZ));
        if (region != null) {
            byte regionChunkX = (byte) (chunkX - regionX * 32);
            byte regionChunkZ = (byte) (chunkZ - regionZ * 32);
            ChunkData chunkData = region.getChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ));
            if (chunkData != null) {
                BlockPosition blockPosition = new BlockPosition(location.blockX(), location.blockY(), location.blockZ());
                return chunkData.isPlacedBlock(blockPosition);
            }
        }
        return false;
    }

    public void handleBlockPlace(ServerLocation block) {
        // Lazy initialize BlockLeveler
        if (blockLeveler == null) {
            blockLeveler = plugin.getLevelManager().getLeveler(BlockLeveler.class);
        }

        SkillSource<BlockXpSource> skillSource = blockLeveler.getSource(block, BlockXpSource.BlockTriggers.BREAK);

        if (skillSource == null) { // Not a source
            return;
        }

        BlockXpSource source = skillSource.source();

        if (!source.checkReplace()) { // Check source option
            return;
        }

        addPlacedBlock(block);
    }

    public void addPlacedBlock(ServerLocation block) {
        Region region = getRegionFromBlock(block);
        // Create region if it does not exist
        if (region == null || region.shouldReload()) {
            addLoadRegionAsync(block);
        } else {
            addToRegion(block, region);
        }
    }

    private void addLoadRegionAsync(ServerLocation block) {
        plugin.getScheduler().executeAsync(() -> {
            Region region = getRegionFromBlock(block);
            String worldName = block.world().key().asString();
            if (region == null) {
                var chunk = block.chunkPosition();
                int regionX = (int) Math.floor((double) chunk.x() / 32.0);
                int regionZ = (int) Math.floor((double) chunk.z() / 32.0);
                region = new Region(worldName, regionX, regionZ);

                RegionCoordinate regionCoordinate = new RegionCoordinate(worldName, regionX, regionZ);
                regions.put(regionCoordinate, region);
            }
            loadRegion(region);
            addToRegion(block, region);
        });
    }

    private void addToRegion(ServerLocation block, Region region) {
        var chunk = block.chunkPosition();
        byte regionChunkX = (byte) (chunk.x() - region.getX() * 32);
        byte regionChunkZ = (byte) (chunk.z() - region.getZ() * 32);
        ChunkData chunkData = region.getChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ));
        // Create chunk data if it does not exist
        if (chunkData == null) {
            chunkData = new ChunkData(region, regionChunkX, regionChunkZ);
            region.setChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ), chunkData);
        }
        chunkData.addPlacedBlock(new BlockPosition(block.blockX(), block.blockY(), block.blockZ()));
    }

    public void removePlacedBlock(ServerLocation block) {
        Region region = getRegionFromBlock(block);
        if (region != null) {
            byte regionChunkX = (byte) (block.chunkPosition().x() - region.getX() * 32);
            byte regionChunkZ = (byte) (block.chunkPosition().z() - region.getZ() * 32);
            ChunkData chunkData = region.getChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ));
            if (chunkData != null) {
                chunkData.removePlacedBlock(new BlockPosition(block.blockX(), block.blockY(), block.blockZ()));
            }
        }
    }

    @Nullable
    private Region getRegionFromBlock(ServerLocation block) {
        int chunkX = block.chunkPosition().x();
        int chunkZ = block.chunkPosition().z();

        int regionX = (int) Math.floor((double) chunkX / 32.0);
        int regionZ = (int) Math.floor((double) chunkZ / 32.0);

        RegionCoordinate regionCoordinate = new RegionCoordinate(block.world().key().asString(), regionX, regionZ);
        return regions.get(regionCoordinate);
    }

    @Override
    public boolean isChunkLoaded(String worldName, int chunkX, int chunkZ) {
        ResourceKey worldKey = ResourceKey.resolve(worldName);
        if (worldKey == null) {
            return false;
        }

        Optional<ServerWorld> optionalWorld = Sponge.server().worldManager().world(worldKey);
        if (optionalWorld.isEmpty()) {
            return false;
        }

        return optionalWorld.get().isChunkLoaded(chunkX, 0, chunkZ, false);
    }
}
