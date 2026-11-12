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
     * Vacuum all dropped items and experience within this bomb's explosion radius,
     * including drops already on the ground and those created by this explosion.
     * Dispenser-fired bombs keep normal drops because they have no player owner.
     */
    static void collectExplosionDrops(BombEntity bomb, Runnable explosion) {
        if (bomb.ticksUntilRemoval != -1
                || !(bomb.getEntityWorld() instanceof ServerWorld world)
                || !(bomb.getOwner() instanceof ServerPlayerEntity player)
                || !player.isAlive()) {
            explosion.run();
            return;
        }

        // Vanilla explosion entity reach is approximately twice the blast power.
        // A spherical check prevents collecting drops outside that reach.
        double radius = bomb.getExplosionPower() * 2.0;
        Vec3d center = bomb.getEntityPos();
        Box area = Box.of(center, radius * 2, radius * 2, radius * 2);

        explosion.run();

        // The explosion has already generated its block drops at this point.
        // Teleport all eligible drops and XP within range to the thrower.
        for (Entity entity : world.getEntitiesByClass(Entity.class, area, NetheriteBombEntity::isCollectible)) {
            if (entity.isAlive() && entity.squaredDistanceTo(center) <= radius * radius) {
                entity.setPosition(player.getX(), player.getY() + 0.5, player.getZ());
                entity.setVelocity(Vec3d.ZERO);
            }
        }
    }

    private static boolean isCollectible(Entity entity) {
        return entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity;
    }
}
