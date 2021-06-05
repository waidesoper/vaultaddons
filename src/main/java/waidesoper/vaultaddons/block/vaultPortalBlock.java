package waidesoper.vaultaddons.block;

import iskallia.vault.Vault;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import waidesoper.vaultaddons.init.ModConfigs;
import waidesoper.vaultaddons.world.CoopRaid;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.VaultEscapeMessage;
import iskallia.vault.world.data.VaultRaidData;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.Optional;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class vaultPortalBlock extends iskallia.vault.block.VaultPortalBlock{
    
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if(world.isRemote || !(entity instanceof PlayerEntity)) return;
        if(entity.isPassenger() || entity.isBeingRidden() || !entity.isNonBoss()) return;

        ServerPlayerEntity player = (ServerPlayerEntity) entity;
        VoxelShape playerVoxel = VoxelShapes.create(player.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ()));

        VaultPortalTileEntity portal = getPortalTileEntity(world, pos);
        String playerBossName = portal == null ? "" : portal.getPlayerBossName();

        if(VoxelShapes.compare(playerVoxel, state.getShape(world, pos), IBooleanFunction.AND)) {
            RegistryKey<World> worldKey = world.getDimensionKey() == Vault.VAULT_KEY ? World.OVERWORLD : Vault.VAULT_KEY;
            ServerWorld destination = ((ServerWorld) world).getServer().getWorld(worldKey);

            if(destination == null) return;

            //Reset cooldown.
            if(player.func_242280_ah()) {
                player.func_242279_ag();
                return;
            }

            world.getServer().runAsync(() -> {
                if (worldKey == World.OVERWORLD) {
                    ServerPlayerEntity playerEntity = (ServerPlayerEntity) entity;
                    CoopRaid raid = (CoopRaid) VaultRaidData.get(destination).getActiveFor(playerEntity);
                    if(raid != null && raid.cannotExit) {
                        StringTextComponent text = new StringTextComponent("You cannot exit this Vault!");
                        text.setStyle(Style.EMPTY.setColor(Color.fromInt(0x00_FF0000)));
                        playerEntity.sendStatusMessage(text, true);
                        return;
                    }

                    BlockPos blockPos = playerEntity.func_241140_K_();
                    Optional<Vector3d> spawn = blockPos == null ? Optional.empty() : PlayerEntity.func_242374_a(destination,
                            blockPos, playerEntity.func_242109_L(), playerEntity.func_241142_M_(), true);

                    if (spawn.isPresent()) {
                        BlockState blockstate = destination.getBlockState(blockPos);
                        Vector3d vector3d = spawn.get();

                        if (!blockstate.isIn(BlockTags.BEDS) && !blockstate.isIn(Blocks.RESPAWN_ANCHOR)) {
                            playerEntity.teleport(destination, vector3d.x, vector3d.y, vector3d.z, playerEntity.func_242109_L(), 0.0F);
                        } else {
                            Vector3d vector3d1 = Vector3d.copyCenteredHorizontally(blockPos).subtract(vector3d).normalize();
                            playerEntity.teleport(destination, vector3d.x, vector3d.y, vector3d.z,
                                    (float) MathHelper.wrapDegrees(MathHelper.atan2(vector3d1.z, vector3d1.x) * (double) (180F / (float) Math.PI) - 90.0D), 0.0F);
                        }

                        ModNetwork.CHANNEL.sendTo(
                                new VaultEscapeMessage(),
                                playerEntity.connection.netManager,
                                NetworkDirection.PLAY_TO_CLIENT
                        );

                    } else {
                        this.moveToSpawn(destination, player);
                    }
                } else if(worldKey == Vault.VAULT_KEY) {
                    if(ModConfigs.VAULT_COOP_ONLY.IS_COOP_ONLY){
                        List<ServerPlayerEntity> players = new ArrayList<>(world.getServer().getPlayerList().getPlayers());
                        VaultRaidData.get(destination).startNew(players,Collections.emptyList(), state.get(RARITY), playerBossName, portal.getData(), false);
                    } else {
                        VaultRaidData.get(destination).startNew(player, state.get(RARITY), playerBossName, portal.getData(), false);
                    }
                }
            });

            if(worldKey == Vault.VAULT_KEY) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }

            player.func_242279_ag();
        }
    }
    
    private void moveToSpawn(ServerWorld world, ServerPlayerEntity player) {
        BlockPos blockpos = world.getSpawnPoint();

        if (world.getDimensionType().hasSkyLight() && world.getServer().getGameType() != GameType.ADVENTURE) {
            int i = Math.max(0, world.getServer().getSpawnRadius(world));
            int j = MathHelper.floor(world.getWorldBorder().getClosestDistance(blockpos.getX(), blockpos.getZ()));
            if (j < i) {
                i = j;
            }

            if (j <= 1) {
                i = 1;
            }

            long k = i * 2 + 1;
            long l = k * k;
            int i1 = l > 2147483647L ? Integer.MAX_VALUE : (int) l;
            int j1 = i1 <= 16 ? i1 - 1 : 17;
            int k1 = (new Random()).nextInt(i1);

            for (int l1 = 0; l1 < i1; ++l1) {
                int i2 = (k1 + j1 * l1) % i1;
                int j2 = i2 % (i * 2 + 1);
                int k2 = i2 / (i * 2 + 1);
                BlockPos blockpos1 = getSpawnPoint(world, blockpos.getX() + j2 - i, blockpos.getZ() + k2 - i, false);
                if (blockpos1 != null) {
                    player.teleport(world, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), 0.0F, 0.0F);

                    if (world.hasNoCollisions(player)) {
                        break;
                    }
                }
            }
        } else {
            player.teleport(world, blockpos.getX(), blockpos.getY(), blockpos.getZ(), 0.0F, 0.0F);
        }
    }

    private VaultPortalTileEntity getPortalTileEntity(World worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        return te instanceof VaultPortalTileEntity ? (VaultPortalTileEntity)te : null;
    }
}
