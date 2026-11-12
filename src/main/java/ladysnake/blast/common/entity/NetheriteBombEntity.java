package ladysnake.blast.common.entity;

import ladysnake.blast.common.init.BlastItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NetheriteBombEntity extends DiamondBombEntity {
    public NetheriteBombEntity(EntityType<? extends BombEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return BlastItems.NETHERITE_BOMB;
    }

    @Override
    protected void explode() {
        collectExplosionDrops(this, super::explode);
    }

    /**
     * Only collect entities created by this explosion. Never steal pre-existing items
     * or experience left by other players. Dispenser-fired bombs keep regular drops.
     */
    static void collectExplosionDrops(BombEntity bomb, Runnable explosion) {
        if (bomb.ticksUntilRemoval != -1
                || !(bomb.getEntityWorld() instanceof ServerWorld world)
                || !(bomb.getOwner() instanceof ServerPlayerEntity player)
                || !player.isAlive()) {
            explosion.run();
            return;
        }

        // Covers the full power-three explosion plus some room for drop scatter.
        Box area = Box.of(bomb.getEntityPos(), 32, 32, 32);
        Set<UUID> existing = new HashSet<>();
        for (Entity entity : world.getEntitiesByClass(Entity.class, area, NetheriteBombEntity::isCollectible)) {
            existing.add(entity.getUuid());
        }

        explosion.run();

        // Teleport drops to the thrower's feet; vanilla pickup handles inventory
        // limits, XP mending and XP orbs naturally on the following server tick.
        for (Entity entity : world.getEntitiesByClass(Entity.class, area, NetheriteBombEntity::isCollectible)) {
            if (!existing.contains(entity.getUuid()) && entity.isAlive()) {
                entity.setPosition(player.getX(), player.getY() + 0.5, player.getZ());
                entity.setVelocity(Vec3d.ZERO);
            }
        }
    }

    private static boolean isCollectible(Entity entity) {
        return entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity;
    }
}
