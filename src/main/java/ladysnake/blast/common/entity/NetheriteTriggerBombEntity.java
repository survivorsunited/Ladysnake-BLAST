package ladysnake.blast.common.entity;

import ladysnake.blast.common.init.BlastItems;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class NetheriteTriggerBombEntity extends DiamondTriggerBombEntity {
    public NetheriteTriggerBombEntity(EntityType<? extends BombEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return BlastItems.NETHERITE_TRIGGER_BOMB;
    }

    @Override
    protected void explode() {
        NetheriteBombEntity.collectExplosionDrops(this, super::explode);
    }
}
