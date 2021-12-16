package com.sammy.malum.common.tile;

import com.sammy.malum.MalumHelper;
import com.sammy.malum.common.block.spirit_altar.IAltarProvider;
import com.sammy.malum.common.item.misc.MalumSpiritItem;
import com.sammy.malum.common.packets.particle.altar.SpiritAltarConsumeParticlePacket;
import com.sammy.malum.common.packets.particle.altar.SpiritAltarCraftParticlePacket;
import com.sammy.malum.common.recipe.SpiritInfusionRecipe;
import com.sammy.malum.core.registry.block.TileEntityRegistry;
import com.sammy.malum.core.registry.item.ItemRegistry;
import com.sammy.malum.core.registry.misc.ParticleRegistry;
import com.sammy.malum.core.registry.misc.SoundRegistry;
import com.sammy.malum.core.systems.blockentity.SimpleBlockEntity;
import com.sammy.malum.core.systems.blockentity.SimpleBlockEntityInventory;
import com.sammy.malum.core.systems.recipe.IngredientWithCount;
import com.sammy.malum.core.systems.recipe.ItemWithCount;
import com.sammy.malum.core.systems.rendering.RenderUtilities;
import com.sammy.malum.core.systems.spirit.SpiritHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collection;
import java.util.Random;

import static com.sammy.malum.core.registry.misc.PacketRegistry.INSTANCE;

public class SpiritAltarTileEntity extends SimpleBlockEntity
{
    public int soundCooldown;
    public int progress;
    public boolean spedUp;
    public int spinUp;
    public float spin;
    public SimpleBlockEntityInventory inventory;
    public SimpleBlockEntityInventory extrasInventory;
    public SimpleBlockEntityInventory spiritInventory;
    public SpiritInfusionRecipe recipe;

    public SpiritAltarTileEntity(BlockPos pos, BlockState state)
    {
        super(TileEntityRegistry.SPIRIT_ALTAR_TILE_ENTITY.get(), pos, state);

        inventory = new SimpleBlockEntityInventory(1, 64, t-> !(t.getItem() instanceof MalumSpiritItem))
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                SpiritAltarTileEntity.this.setChanged();
                setChanged();
                recipe = SpiritInfusionRecipe.getRecipeForAltar(level, inventory.getStackInSlot(0), spiritInventory.nonEmptyStacks());
                MalumHelper.updateAndNotifyState(level, worldPosition);
            }
        };
        extrasInventory = new SimpleBlockEntityInventory(8, 1)
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                SpiritAltarTileEntity.this.setChanged();
                setChanged();
                MalumHelper.updateAndNotifyState(level, worldPosition);
            }
        };
        spiritInventory = new SimpleBlockEntityInventory(8, 64, t-> t.getItem() instanceof MalumSpiritItem)
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                SpiritAltarTileEntity.this.setChanged();
                setChanged();
                recipe = SpiritInfusionRecipe.getRecipeForAltar(level, inventory.getStackInSlot(0), spiritInventory.nonEmptyStacks());
                MalumHelper.updateAndNotifyState(level, worldPosition);
            }
        };
    }

    @Override
    public void onBreak() {
        SpiritAltarTileEntity tileEntity = (SpiritAltarTileEntity) level.getBlockEntity(worldPosition);
        level.addFreshEntity(new ItemEntity(level,worldPosition.getX()+0.5f,worldPosition.getY()+0.5f,worldPosition.getZ()+0.5f,tileEntity.inventory.getStackInSlot(0)));
        for (ItemStack itemStack : tileEntity.spiritInventory.stacks())
        {
            level.addFreshEntity(new ItemEntity(level,worldPosition.getX()+0.5f,worldPosition.getY()+0.5f,worldPosition.getZ()+0.5f,itemStack));
        }
        for (ItemStack itemStack : tileEntity.extrasInventory.stacks())
        {
            level.addFreshEntity(new ItemEntity(level,worldPosition.getX()+0.5f,worldPosition.getY()+0.5f,worldPosition.getZ()+0.5f,itemStack));
        }
    }

    @Override
    public InteractionResult onUse(Player player, InteractionHand hand) {
        if (level.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }
        if (hand.equals(InteractionHand.MAIN_HAND))
        {
            if (level.getBlockEntity(worldPosition) instanceof SpiritAltarTileEntity)
            {
                SpiritAltarTileEntity tileEntity = (SpiritAltarTileEntity) level.getBlockEntity(worldPosition);
                ItemStack heldStack = player.getMainHandItem();
                if (heldStack.getItem().equals(ItemRegistry.HEX_ASH.get()) && !tileEntity.inventory.getStackInSlot(0).isEmpty())
                {
                    if (!tileEntity.spedUp)
                    {
                        heldStack.shrink(1);
                        tileEntity.progress = 0;
                        tileEntity.spedUp = true;
                        level.playSound(null, worldPosition, SoundRegistry.ALTAR_SPEED_UP, SoundSource.BLOCKS,1,0.9f + level.random.nextFloat() * 0.2f);
                        MalumHelper.updateState(level, worldPosition);
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.PASS;
                }
                if (!(heldStack.getItem() instanceof MalumSpiritItem))
                {
                    boolean success = tileEntity.inventory.playerHandleItem(level, player, hand);
                    if (success)
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                if (heldStack.getItem() instanceof MalumSpiritItem || heldStack.isEmpty())
                {
                    boolean success = tileEntity.spiritInventory.playerHandleItem(level, player, hand);
                    if (success)
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return super.onUse(player, hand);
    }

    @Override
    public CompoundTag save(CompoundTag compound)
    {
        if (progress != 0)
        {
            compound.putInt("progress", progress);
        }
        if (spinUp != 0)
        {
            compound.putInt("spinUp", spinUp);
        }
        if (spedUp)
        {
            compound.putByte("spedUp", (byte) 0);
        }
        
        inventory.save(compound);
        spiritInventory.save(compound, "spiritInventory");
        extrasInventory.save(compound, "extrasInventory");
        return super.save(compound);
    }
    
    @Override
    public void load(CompoundTag compound)
    {
        progress = compound.getInt("progress");
        spinUp = compound.getInt("spinUp");
        if (compound.contains("spedUp"))
        {
            spedUp = true;
        }
        inventory.load(compound);
        spiritInventory.load(compound, "spiritInventory");
        extrasInventory.load(compound, "extrasInventory");
        recipe = SpiritInfusionRecipe.getRecipeForAltar(level, inventory.getStackInSlot(0), spiritInventory.nonEmptyStacks());
    }
    
    public void tick()
    {
        if (soundCooldown > 0)
        {
            soundCooldown--;
        }
        if (recipe != null)
        {
            int spinCap = spedUp ? 30 : 10;
            if (spinUp < spinCap)
            {
                spinUp++;
            }
            if (!level.isClientSide)
            {
                if (soundCooldown == 0)
                {
                    level.playSound(null, worldPosition, SoundRegistry.ALTAR_LOOP, SoundSource.BLOCKS, 1, 1f);
                    soundCooldown = 180;
                }
                progress++;
                int progressCap = spedUp ? 60 : 360;
                if (progress >= progressCap)
                {
                    boolean success = consume();
                    if (success)
                    {
                        craft();
                    }
                }
            }
        }
        else
        {
            progress = 0;
            if (spinUp > 0)
            {
                spinUp--;
            }
            spedUp = false;
        }
        if (level.isClientSide)
        {
            passiveParticles();
        }
    }
    public static Vec3 itemPos(SpiritAltarTileEntity tileEntity)
    {
        return MalumHelper.fromBlockPos(tileEntity.getBlockPos()).add(tileEntity.itemOffset());
    }
    public Vec3 itemOffset()
    {
        return new Vec3(0.5f, 1.25f, 0.5f);
    }
    public static Vec3 itemOffset(SpiritAltarTileEntity tileEntity, int slot)
    {
        float distance = 1 - Math.min(0.25f, tileEntity.spinUp / 40f) + (float)Math.sin(tileEntity.spin/20f)*0.025f;
        float height = 0.75f + Math.min(0.5f, tileEntity.spinUp / 20f);
        return MalumHelper.rotatedCirclePosition(new Vec3(0.5f,height,0.5f), distance,slot, tileEntity.spiritInventory.nonEmptyItems(), (long)tileEntity.spin,360);
    }
    public boolean consume()
    {
        Vec3 itemPos = itemPos(this);
        int extras = extrasInventory.nonEmptyItems();
        if (extras != recipe.extraItems.size())
        {
            progress *= 0.5f;
            int horizontal = 4;
            int vertical = 2;
            Collection<BlockPos> nearbyBlocks = MalumHelper.getBlocks(worldPosition, horizontal, vertical, horizontal);
            for (BlockPos pos : nearbyBlocks)
            {
                if (level.getBlockEntity(pos) instanceof IAltarProvider)
                {
                    IAltarProvider tileEntity = (IAltarProvider) level.getBlockEntity(pos);
                    ItemStack providedStack = tileEntity.providedInventory().getStackInSlot(0);
                    IngredientWithCount requestedItem = recipe.extraItems.get(extras);
                    if (requestedItem.matches(providedStack))
                    {
                        level.playSound(null, pos, SoundRegistry.ALTAR_CONSUME, SoundSource.BLOCKS, 1, 0.9f + level.random.nextFloat() * 0.2f);
                        Vec3 providedItemPos = tileEntity.providedItemPos();
                        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(()->level.getChunkAt(pos)), SpiritAltarConsumeParticlePacket.fromSpirits(providedStack, recipe.getSpirits(), providedItemPos.x,providedItemPos.y,providedItemPos.z, itemPos.x,itemPos.y,itemPos.z));
                        extrasInventory.playerInsertItem(level, providedStack.split(requestedItem.count));
                        MalumHelper.updateAndNotifyState(level, pos);
                        MalumHelper.updateAndNotifyState(level, this.worldPosition);
                        break;
                    }
                }
            }
            return false;
        }
        return true;
    }
    public void craft()
    {
        ItemStack stack = inventory.getStackInSlot(0);
        Vec3 itemPos = itemPos(this);
        ItemStack outputStack = recipe.output.stack();
        if (inventory.getStackInSlot(0).hasTag())
        {
            outputStack.setTag(stack.getTag());
        }
        if (!recipe.retainsPrimeItem) {
            stack.shrink(recipe.input.count);
        }
        for (ItemWithCount spirit : recipe.spirits)
        {
            for (int i = 0; i < spiritInventory.slotCount; i++)
            {
                ItemStack spiritStack = spiritInventory.getStackInSlot(i);
                if (spirit.matches(spiritStack))
                {
                    spiritStack.shrink(spirit.count);
                    break;
                }
            }
        }

        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(()->level.getChunkAt(worldPosition)), SpiritAltarCraftParticlePacket.fromSpirits(recipe.getSpirits(), itemPos.x, itemPos.y, itemPos.z));
        progress = 0;
        extrasInventory.clearItems();
        recipe = SpiritInfusionRecipe.getRecipeForAltar(level, stack, spiritInventory.nonEmptyStacks());
        level.playSound(null, worldPosition, SoundRegistry.ALTAR_CRAFT, SoundSource.BLOCKS, 1, 0.9f + level.random.nextFloat() * 0.2f);
        level.addFreshEntity(new ItemEntity(level, itemPos.x, itemPos.y, itemPos.z, outputStack));

        MalumHelper.updateAndNotifyState(level, worldPosition);
    }
    public void passiveParticles()
    {
        Vec3 itemPos = itemPos(this);
        spin += 1+ spinUp / 5f;
        for (int i = 0; i < spiritInventory.slotCount; i++)
        {
            ItemStack item = spiritInventory.getStackInSlot(i);
            if (item.getItem() instanceof MalumSpiritItem)
            {
                Vec3 offset = itemOffset(this, i);
                Random rand = level.random;
                double x = getBlockPos().getX() + offset.x();
                double y = getBlockPos().getY() + offset.y();
                double z = getBlockPos().getZ() + offset.z();
                MalumSpiritItem spiritSplinterItem = (MalumSpiritItem) item.getItem();
                Color color = spiritSplinterItem.type.color;
                SpiritHelper.spawnSpiritParticles(level, x,y,z, color);


                if (recipe != null)
                {
                    Vec3 velocity = new Vec3(x, y, z).subtract(itemPos).normalize().scale(-0.03f);
                    RenderUtilities.create(ParticleRegistry.WISP_PARTICLE)
                            .setAlpha(0.15f, 0f)
                            .setLifetime(40)
                            .setScale(0.2f, 0)
                            .randomOffset(0.02f)
                            .randomVelocity(0.01f, 0.01f)
                            .setColor(color, color.darker())
                            .randomVelocity(0.0025f, 0.0025f)
                            .addVelocity(velocity.x, velocity.y, velocity.z)
                            .enableNoClip()
                            .repeat(level, x, y, z, spedUp ? 4 : 2);

                    float alpha = 0.08f / spiritInventory.nonEmptyItems();
                    RenderUtilities.create(ParticleRegistry.SPARKLE_PARTICLE)
                            .setAlpha(alpha, 0f)
                            .setLifetime(20)
                            .setScale(0.5f, 0)
                            .randomOffset(0.1, 0.1)
                            .randomVelocity(0.02f, 0.02f)
                            .setColor(color, color.darker())
                            .randomVelocity(0.0025f, 0.0025f)
                            .enableNoClip()
                            .repeat(level, itemPos.x,itemPos.y,itemPos.z,spedUp ? 4 : 2);
                }
            }
        }
    }
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return inventory.inventoryOptional.cast();
        }
        return super.getCapability(cap);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return inventory.inventoryOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}