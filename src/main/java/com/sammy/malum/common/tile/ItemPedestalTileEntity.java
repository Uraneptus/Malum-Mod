package com.sammy.malum.common.tile;

import com.sammy.malum.MalumHelper;
import com.sammy.malum.common.block.spirit_altar.IAltarProvider;
import com.sammy.malum.common.item.misc.MalumSpiritItem;
import com.sammy.malum.core.registry.block.TileEntityRegistry;
import com.sammy.malum.core.systems.blockentity.SimpleBlockEntityInventory;
import com.sammy.malum.core.systems.blockentity.SimpleInventoryBlockEntity;
import com.sammy.malum.core.systems.spirit.SpiritHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class ItemPedestalTileEntity extends SimpleInventoryBlockEntity implements IAltarProvider
{
    public ItemPedestalTileEntity(BlockPos pos, BlockState state)
    {
        super(TileEntityRegistry.ITEM_PEDESTAL_TILE_ENTITY.get(), pos, state);
        inventory = new SimpleBlockEntityInventory(1, 64)
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                ItemPedestalTileEntity.this.setChanged();
                setChanged();
                MalumHelper.updateAndNotifyState(level, worldPosition);
            }
        };
    }

    @Override
    public SimpleBlockEntityInventory providedInventory()
    {
        return inventory;
    }
    @Override
    public Vec3 providedItemPos()
    {
        return itemPos(this);
    }
    public static Vec3 itemPos(SimpleInventoryBlockEntity tileEntity)
    {
        return MalumHelper.fromBlockPos(tileEntity.getBlockPos()).add(itemOffset());
    }
    public static Vec3 itemOffset()
    {
        return new Vec3(0.5f, 1.1f, 0.5f);
    }

    public void tick()
    {
        if (level.isClientSide) {
            if (inventory.getStackInSlot(0).getItem() instanceof MalumSpiritItem) {
                MalumSpiritItem item = (MalumSpiritItem) inventory.getStackInSlot(0).getItem();
                Color color = item.type.color;
                Vec3 pos = itemPos(this);
                double x = pos.x;
                double y = pos.y + Math.sin((level.getGameTime() % 360) / 20f) * 0.1f;
                double z = pos.z;
                SpiritHelper.spawnSpiritParticles(level, x, y, z, color);
            }
        }
    }
}
