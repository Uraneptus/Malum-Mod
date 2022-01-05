package com.sammy.malum.common.enchantment;

import com.sammy.malum.core.registry.enchantment.MalumEnchantments;
import com.sammy.malum.core.registry.DamageSourceRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;

public class HauntedEnchantment extends Enchantment
{
    public HauntedEnchantment()
    {
        super(Rarity.UNCOMMON, MalumEnchantments.SCYTHE, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }
    
    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level)
    {
        if (target instanceof LivingEntity entity)
        {
            if (entity.isAlive()) {
                entity.invulnerableTime = 0;
                entity.hurt(DamageSourceRegistry.causeNoShatterVoodooDamage(user), level + 1);
            }
        }
        super.doPostAttack(user, target, level);
    }

    @Override
    public int getMaxLevel()
    {
        return 2;
    }
    
}
