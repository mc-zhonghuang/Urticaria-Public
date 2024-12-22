package net.minecraft.server.management;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.optifine.ChunkPosComparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

public class PlayerManager {
    private static final Logger pmLogger = LogManager.getLogger();
    private final WorldServer theWorldServer;
    private final List<EntityPlayerMP> players = Lists.newArrayList();
    private final LongHashMap<PlayerManager.PlayerInstance> playerInstances = new LongHashMap();
    private final List<PlayerManager.PlayerInstance> playerInstancesToUpdate = Lists.newArrayList();
    private final List<PlayerManager.PlayerInstance> playerInstanceList = Lists.newArrayList();

    /**
     * Number of chunks the server sends to the client. Valid 3<=x<=15. In server.properties.
     */
    private int playerViewRadius;

    /**
     * time what is using to check if InhabitedTime should be calculated
     */
    private long previousTotalWorldTime;

    /**
     * x, z direction vectors: east, south, west, north
     */
    private final int[][] xzDirectionsConst = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    private final Map<EntityPlayerMP, Set<ChunkCoordIntPair>> mapPlayerPendingEntries = new HashMap();

    public PlayerManager(final WorldServer serverWorld) {
        this.theWorldServer = serverWorld;
        this.setPlayerViewRadius(serverWorld.getMinecraftServer().getConfigurationManager().getViewDistance());
    }

    /**
     * Returns the WorldServer associated with this PlayerManager
     */
    public WorldServer getWorldServer() {
        return this.theWorldServer;
    }

    /**
     * updates all the player instances that need to be updated
     */
    public void updatePlayerInstances() {
        final Set<Entry<EntityPlayerMP, Set<ChunkCoordIntPair>>> set = this.mapPlayerPendingEntries.entrySet();
        final Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            final Entry<EntityPlayerMP, Set<ChunkCoordIntPair>> entry = (Entry) iterator.next();
            final Set<ChunkCoordIntPair> set1 = entry.getValue();

            if (!set1.isEmpty()) {
                final EntityPlayerMP entityplayermp = entry.getKey();

                if (entityplayermp.worldObj != this.theWorldServer) {
                    iterator.remove();
                } else {
                    int i = this.playerViewRadius / 3 + 1;

                    if (!Config.isLazyChunkLoading()) {
                        i = this.playerViewRadius * 2 + 1;
                    }

                    for (final ChunkCoordIntPair chunkcoordintpair : this.getNearest(set1, entityplayermp, i)) {
                        final PlayerManager.PlayerInstance playermanager$playerinstance = this.getPlayerInstance(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos, true);
                        playermanager$playerinstance.addPlayer(entityplayermp);
                        set1.remove(chunkcoordintpair);
                    }
                }
            }
        }

        final long j = this.theWorldServer.getTotalWorldTime();

        if (j - this.previousTotalWorldTime > 8000L) {
            this.previousTotalWorldTime = j;

            for (int k = 0; k < this.playerInstanceList.size(); ++k) {
                final PlayerManager.PlayerInstance playermanager$playerinstance1 = this.playerInstanceList.get(k);
                playermanager$playerinstance1.onUpdate();
                playermanager$playerinstance1.processChunk();
            }
        } else {
            for (int l = 0; l < this.playerInstancesToUpdate.size(); ++l) {
                final PlayerManager.PlayerInstance playermanager$playerinstance2 = this.playerInstancesToUpdate.get(l);
                playermanager$playerinstance2.onUpdate();
            }
        }

        this.playerInstancesToUpdate.clear();

        if (this.players.isEmpty()) {
            final WorldProvider worldprovider = this.theWorldServer.provider;

            if (!worldprovider.canRespawnHere()) {
                this.theWorldServer.theChunkProviderServer.unloadAllChunks();
            }
        }
    }

    public boolean hasPlayerInstance(final int chunkX, final int chunkZ) {
        final long i = (long) chunkX + 2147483647L | (long) chunkZ + 2147483647L << 32;
        return this.playerInstances.getValueByKey(i) != null;
    }

    /**
     * passi n the chunk x and y and a flag as to whether or not the instance should be made if it doesnt exist
     *
     * @param chunkX         The chunk X coordinate
     * @param chunkZ         The chunk Z coordinate
     * @param createIfAbsent If the player instance should be created if it doesn't exist
     */
    private PlayerManager.PlayerInstance getPlayerInstance(final int chunkX, final int chunkZ, final boolean createIfAbsent) {
        final long i = (long) chunkX + 2147483647L | (long) chunkZ + 2147483647L << 32;
        PlayerManager.PlayerInstance playermanager$playerinstance = this.playerInstances.getValueByKey(i);

        if (playermanager$playerinstance == null && createIfAbsent) {
            playermanager$playerinstance = new PlayerManager.PlayerInstance(chunkX, chunkZ);
            this.playerInstances.add(i, playermanager$playerinstance);
            this.playerInstanceList.add(playermanager$playerinstance);
        }

        return playermanager$playerinstance;
    }

    public void markBlockForUpdate(final BlockPos pos) {
        final int i = pos.getX() >> 4;
        final int j = pos.getZ() >> 4;
        final PlayerManager.PlayerInstance playermanager$playerinstance = this.getPlayerInstance(i, j, false);

        if (playermanager$playerinstance != null) {
            playermanager$playerinstance.flagChunkForUpdate(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
        }
    }

    /**
     * Adds an EntityPlayerMP to the PlayerManager and to all player instances within player visibility
     *
     * @param player The player to add
     */
    public void addPlayer(final EntityPlayerMP player) {
        final int i = (int) player.posX >> 4;
        final int j = (int) player.posZ >> 4;
        player.managedPosX = player.posX;
        player.managedPosZ = player.posZ;
        final int k = Math.min(this.playerViewRadius, 8);
        final int l = i - k;
        final int i1 = i + k;
        final int j1 = j - k;
        final int k1 = j + k;
        final Set<ChunkCoordIntPair> set = this.getPendingEntriesSafe(player);

        for (int l1 = i - this.playerViewRadius; l1 <= i + this.playerViewRadius; ++l1) {
            for (int i2 = j - this.playerViewRadius; i2 <= j + this.playerViewRadius; ++i2) {
                if (l1 >= l && l1 <= i1 && i2 >= j1 && i2 <= k1) {
                    this.getPlayerInstance(l1, i2, true).addPlayer(player);
                } else {
                    set.add(new ChunkCoordIntPair(l1, i2));
                }
            }
        }

        this.players.add(player);
        this.filterChunkLoadQueue(player);
    }

    /**
     * Removes all chunks from the given player's chunk load queue that are not in viewing range of the player.
     */
    public void filterChunkLoadQueue(final EntityPlayerMP player) {
        final List<ChunkCoordIntPair> list = Lists.newArrayList(player.loadedChunks);
        int i = 0;
        final int j = this.playerViewRadius;
        final int k = (int) player.posX >> 4;
        final int l = (int) player.posZ >> 4;
        int i1 = 0;
        int j1 = 0;
        ChunkCoordIntPair chunkcoordintpair = this.getPlayerInstance(k, l, true).chunkCoords;
        player.loadedChunks.clear();

        if (list.contains(chunkcoordintpair)) {
            player.loadedChunks.add(chunkcoordintpair);
        }

        for (int k1 = 1; k1 <= j * 2; ++k1) {
            for (int l1 = 0; l1 < 2; ++l1) {
                final int[] aint = this.xzDirectionsConst[i++ % 4];

                for (int i2 = 0; i2 < k1; ++i2) {
                    i1 += aint[0];
                    j1 += aint[1];
                    chunkcoordintpair = this.getPlayerInstance(k + i1, l + j1, true).chunkCoords;

                    if (list.contains(chunkcoordintpair)) {
                        player.loadedChunks.add(chunkcoordintpair);
                    }
                }
            }
        }

        i = i % 4;

        for (int j2 = 0; j2 < j * 2; ++j2) {
            i1 += this.xzDirectionsConst[i][0];
            j1 += this.xzDirectionsConst[i][1];
            chunkcoordintpair = this.getPlayerInstance(k + i1, l + j1, true).chunkCoords;

            if (list.contains(chunkcoordintpair)) {
                player.loadedChunks.add(chunkcoordintpair);
            }
        }
    }

    /**
     * Removes an EntityPlayerMP from the PlayerManager.
     *
     * @param player The player to remove
     */
    public void removePlayer(final EntityPlayerMP player) {
        this.mapPlayerPendingEntries.remove(player);
        final int i = (int) player.managedPosX >> 4;
        final int j = (int) player.managedPosZ >> 4;

        for (int k = i - this.playerViewRadius; k <= i + this.playerViewRadius; ++k) {
            for (int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l) {
                final PlayerManager.PlayerInstance playermanager$playerinstance = this.getPlayerInstance(k, l, false);

                if (playermanager$playerinstance != null) {
                    playermanager$playerinstance.removePlayer(player);
                }
            }
        }

        this.players.remove(player);
    }

    /**
     * Determine if two rectangles centered at the given points overlap for the provided radius. Arguments: x1, z1, x2,
     * z2, radius.
     *
     * @param x1     The first X coordinate
     * @param z1     The first Z coordinate
     * @param x2     The second X coordinate
     * @param z2     The second Z coordinate
     * @param radius The radius
     */
    private boolean overlaps(final int x1, final int z1, final int x2, final int z2, final int radius) {
        final int i = x1 - x2;
        final int j = z1 - z2;
        return i >= -radius && i <= radius && j >= -radius && j <= radius;
    }

    /**
     * update chunks around a player being moved by server logic (e.g. cart, boat)
     *
     * @param player The player to update chunks around
     */
    public void updateMountedMovingPlayer(final EntityPlayerMP player) {
        final int i = (int) player.posX >> 4;
        final int j = (int) player.posZ >> 4;
        final double d0 = player.managedPosX - player.posX;
        final double d1 = player.managedPosZ - player.posZ;
        final double d2 = d0 * d0 + d1 * d1;

        if (d2 >= 64.0D) {
            final int k = (int) player.managedPosX >> 4;
            final int l = (int) player.managedPosZ >> 4;
            final int i1 = this.playerViewRadius;
            final int j1 = i - k;
            final int k1 = j - l;

            if (j1 != 0 || k1 != 0) {
                final Set<ChunkCoordIntPair> set = this.getPendingEntriesSafe(player);

                for (int l1 = i - i1; l1 <= i + i1; ++l1) {
                    for (int i2 = j - i1; i2 <= j + i1; ++i2) {
                        if (!this.overlaps(l1, i2, k, l, i1)) {
                            if (Config.isLazyChunkLoading()) {
                                set.add(new ChunkCoordIntPair(l1, i2));
                            } else {
                                this.getPlayerInstance(l1, i2, true).addPlayer(player);
                            }
                        }

                        if (!this.overlaps(l1 - j1, i2 - k1, i, j, i1)) {
                            set.remove(new ChunkCoordIntPair(l1 - j1, i2 - k1));
                            final PlayerManager.PlayerInstance playermanager$playerinstance = this.getPlayerInstance(l1 - j1, i2 - k1, false);

                            if (playermanager$playerinstance != null) {
                                playermanager$playerinstance.removePlayer(player);
                            }
                        }
                    }
                }

                this.filterChunkLoadQueue(player);
                player.managedPosX = player.posX;
                player.managedPosZ = player.posZ;
            }
        }
    }

    public boolean isPlayerWatchingChunk(final EntityPlayerMP player, final int chunkX, final int chunkZ) {
        final PlayerManager.PlayerInstance playermanager$playerinstance = this.getPlayerInstance(chunkX, chunkZ, false);
        return playermanager$playerinstance != null && playermanager$playerinstance.playersWatchingChunk.contains(player) && !player.loadedChunks.contains(playermanager$playerinstance.chunkCoords);
    }

    public void setPlayerViewRadius(int radius) {
        radius = MathHelper.clamp_int(radius, 3, 64);

        if (radius != this.playerViewRadius) {
            final int i = radius - this.playerViewRadius;

            for (final EntityPlayerMP entityplayermp : Lists.newArrayList(this.players)) {
                final int j = (int) entityplayermp.posX >> 4;
                final int k = (int) entityplayermp.posZ >> 4;
                final Set<ChunkCoordIntPair> set = this.getPendingEntriesSafe(entityplayermp);

                if (i > 0) {
                    for (int j1 = j - radius; j1 <= j + radius; ++j1) {
                        for (int k1 = k - radius; k1 <= k + radius; ++k1) {
                            if (Config.isLazyChunkLoading()) {
                                set.add(new ChunkCoordIntPair(j1, k1));
                            } else {
                                final PlayerManager.PlayerInstance playermanager$playerinstance1 = this.getPlayerInstance(j1, k1, true);

                                if (!playermanager$playerinstance1.playersWatchingChunk.contains(entityplayermp)) {
                                    playermanager$playerinstance1.addPlayer(entityplayermp);
                                }
                            }
                        }
                    }
                } else {
                    for (int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l) {
                        for (int i1 = k - this.playerViewRadius; i1 <= k + this.playerViewRadius; ++i1) {
                            if (!this.overlaps(l, i1, j, k, radius)) {
                                set.remove(new ChunkCoordIntPair(l, i1));
                                final PlayerManager.PlayerInstance playermanager$playerinstance = this.getPlayerInstance(l, i1, true);

                                if (playermanager$playerinstance != null) {
                                    playermanager$playerinstance.removePlayer(entityplayermp);
                                }
                            }
                        }
                    }
                }
            }

            this.playerViewRadius = radius;
        }
    }

    /**
     * Get the furthest viewable block given player's view distance
     */
    public static int getFurthestViewableBlock(final int distance) {
        return distance * 16 - 16;
    }

    private PriorityQueue<ChunkCoordIntPair> getNearest(final Set<ChunkCoordIntPair> p_getNearest_1_, final EntityPlayerMP p_getNearest_2_, final int p_getNearest_3_) {
        float f;

        for (f = p_getNearest_2_.rotationYaw + 90.0F; f <= -180.0F; f += 360.0F) {
        }

        while (f > 180.0F) {
            f -= 360.0F;
        }

        final double d0 = (double) f * 0.017453292519943295D;
        final double d1 = p_getNearest_2_.rotationPitch;
        final double d2 = d1 * 0.017453292519943295D;
        final ChunkPosComparator chunkposcomparator = new ChunkPosComparator(p_getNearest_2_.chunkCoordX, p_getNearest_2_.chunkCoordZ, d0, d2);
        final Comparator<ChunkCoordIntPair> comparator = Collections.reverseOrder(chunkposcomparator);
        final PriorityQueue<ChunkCoordIntPair> priorityqueue = new PriorityQueue(p_getNearest_3_, comparator);

        for (final ChunkCoordIntPair chunkcoordintpair : p_getNearest_1_) {
            if (priorityqueue.size() < p_getNearest_3_) {
                priorityqueue.add(chunkcoordintpair);
            } else {
                final ChunkCoordIntPair chunkcoordintpair1 = priorityqueue.peek();

                if (chunkposcomparator.compare(chunkcoordintpair, chunkcoordintpair1) < 0) {
                    priorityqueue.remove();
                    priorityqueue.add(chunkcoordintpair);
                }
            }
        }

        return priorityqueue;
    }

    private Set<ChunkCoordIntPair> getPendingEntriesSafe(final EntityPlayerMP p_getPendingEntriesSafe_1_) {
        final Set<ChunkCoordIntPair> set = this.mapPlayerPendingEntries.get(p_getPendingEntriesSafe_1_);

        if (set != null) {
            return set;
        } else {
            final int i = Math.min(this.playerViewRadius, 8);
            final int j = this.playerViewRadius * 2 + 1;
            final int k = i * 2 + 1;
            int l = j * j - k * k;
            l = Math.max(l, 16);
            final HashSet hashset = new HashSet(l);
            this.mapPlayerPendingEntries.put(p_getPendingEntriesSafe_1_, hashset);
            return hashset;
        }
    }

    class PlayerInstance {
        private final List<EntityPlayerMP> playersWatchingChunk = Lists.newArrayList();
        private final ChunkCoordIntPair chunkCoords;
        private final short[] locationOfBlockChange = new short[64];
        private int numBlocksToUpdate;
        private int flagsYAreasToUpdate;
        private long previousWorldTime;

        public PlayerInstance(final int chunkX, final int chunkZ) {
            this.chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);
            PlayerManager.this.getWorldServer().theChunkProviderServer.loadChunk(chunkX, chunkZ);
        }

        public void addPlayer(final EntityPlayerMP player) {
            if (this.playersWatchingChunk.contains(player)) {
                PlayerManager.pmLogger.debug("Failed to add player. {} already is in chunk {}, {}", new Object[]{player, Integer.valueOf(this.chunkCoords.chunkXPos), Integer.valueOf(this.chunkCoords.chunkZPos)});
            } else {
                if (this.playersWatchingChunk.isEmpty()) {
                    this.previousWorldTime = PlayerManager.this.theWorldServer.getTotalWorldTime();
                }

                this.playersWatchingChunk.add(player);
                player.loadedChunks.add(this.chunkCoords);
            }
        }

        public void removePlayer(final EntityPlayerMP player) {
            if (this.playersWatchingChunk.contains(player)) {
                final Chunk chunk = PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos);

                if (chunk.isPopulated()) {
                    player.playerNetServerHandler.sendPacket(new S21PacketChunkData(chunk, true, 0));
                }

                this.playersWatchingChunk.remove(player);
                player.loadedChunks.remove(this.chunkCoords);

                if (this.playersWatchingChunk.isEmpty()) {
                    final long i = (long) this.chunkCoords.chunkXPos + 2147483647L | (long) this.chunkCoords.chunkZPos + 2147483647L << 32;
                    this.increaseInhabitedTime(chunk);
                    PlayerManager.this.playerInstances.remove(i);
                    PlayerManager.this.playerInstanceList.remove(this);

                    if (this.numBlocksToUpdate > 0) {
                        PlayerManager.this.playerInstancesToUpdate.remove(this);
                    }

                    PlayerManager.this.getWorldServer().theChunkProviderServer.dropChunk(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos);
                }
            }
        }

        public void processChunk() {
            this.increaseInhabitedTime(PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos));
        }

        private void increaseInhabitedTime(final Chunk theChunk) {
            theChunk.setInhabitedTime(theChunk.getInhabitedTime() + PlayerManager.this.theWorldServer.getTotalWorldTime() - this.previousWorldTime);
            this.previousWorldTime = PlayerManager.this.theWorldServer.getTotalWorldTime();
        }

        public void flagChunkForUpdate(final int x, final int y, final int z) {
            if (this.numBlocksToUpdate == 0) {
                PlayerManager.this.playerInstancesToUpdate.add(this);
            }

            this.flagsYAreasToUpdate |= 1 << (y >> 4);

            if (this.numBlocksToUpdate < 64) {
                final short short1 = (short) (x << 12 | z << 8 | y);

                for (int i = 0; i < this.numBlocksToUpdate; ++i) {
                    if (this.locationOfBlockChange[i] == short1) {
                        return;
                    }
                }

                this.locationOfBlockChange[this.numBlocksToUpdate++] = short1;
            }
        }

        public void sendToAllPlayersWatchingChunk(final Packet thePacket) {
            for (int i = 0; i < this.playersWatchingChunk.size(); ++i) {
                final EntityPlayerMP entityplayermp = this.playersWatchingChunk.get(i);

                if (!entityplayermp.loadedChunks.contains(this.chunkCoords)) {
                    entityplayermp.playerNetServerHandler.sendPacket(thePacket);
                }
            }
        }

        public void onUpdate() {
            if (this.numBlocksToUpdate != 0) {
                if (this.numBlocksToUpdate == 1) {
                    final int k1 = (this.locationOfBlockChange[0] >> 12 & 15) + this.chunkCoords.chunkXPos * 16;
                    final int i2 = this.locationOfBlockChange[0] & 255;
                    final int k2 = (this.locationOfBlockChange[0] >> 8 & 15) + this.chunkCoords.chunkZPos * 16;
                    final BlockPos blockpos = new BlockPos(k1, i2, k2);
                    this.sendToAllPlayersWatchingChunk(new S23PacketBlockChange(PlayerManager.this.theWorldServer, blockpos));

                    if (PlayerManager.this.theWorldServer.getBlockState(blockpos).getBlock().hasTileEntity()) {
                        this.sendTileToAllPlayersWatchingChunk(PlayerManager.this.theWorldServer.getTileEntity(blockpos));
                    }
                } else if (this.numBlocksToUpdate != 64) {
                    this.sendToAllPlayersWatchingChunk(new S22PacketMultiBlockChange(this.numBlocksToUpdate, this.locationOfBlockChange, PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos)));

                    for (int j1 = 0; j1 < this.numBlocksToUpdate; ++j1) {
                        final int l1 = (this.locationOfBlockChange[j1] >> 12 & 15) + this.chunkCoords.chunkXPos * 16;
                        final int j2 = this.locationOfBlockChange[j1] & 255;
                        final int l2 = (this.locationOfBlockChange[j1] >> 8 & 15) + this.chunkCoords.chunkZPos * 16;
                        final BlockPos blockpos1 = new BlockPos(l1, j2, l2);

                        if (PlayerManager.this.theWorldServer.getBlockState(blockpos1).getBlock().hasTileEntity()) {
                            this.sendTileToAllPlayersWatchingChunk(PlayerManager.this.theWorldServer.getTileEntity(blockpos1));
                        }
                    }
                } else {
                    final int i = this.chunkCoords.chunkXPos * 16;
                    final int j = this.chunkCoords.chunkZPos * 16;
                    this.sendToAllPlayersWatchingChunk(new S21PacketChunkData(PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos), false, this.flagsYAreasToUpdate));

                    for (int k = 0; k < 16; ++k) {
                        if ((this.flagsYAreasToUpdate & 1 << k) != 0) {
                            final int l = k << 4;
                            final List<TileEntity> list = PlayerManager.this.theWorldServer.getTileEntitiesIn(i, l, j, i + 16, l + 16, j + 16);

                            for (int i1 = 0; i1 < list.size(); ++i1) {
                                this.sendTileToAllPlayersWatchingChunk(list.get(i1));
                            }
                        }
                    }
                }

                this.numBlocksToUpdate = 0;
                this.flagsYAreasToUpdate = 0;
            }
        }

        private void sendTileToAllPlayersWatchingChunk(final TileEntity theTileEntity) {
            if (theTileEntity != null) {
                final Packet packet = theTileEntity.getDescriptionPacket();

                if (packet != null) {
                    this.sendToAllPlayersWatchingChunk(packet);
                }
            }
        }
    }
}
