package com.carpentersblocks.network;

import java.io.IOException;

import com.carpentersblocks.block.BlockCarpentersSlope;
import com.carpentersblocks.util.registry.BlockRegistry;

import io.netty.buffer.ByteBufInputStream;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class PacketSlopeSelect implements ICarpentersPacket 
{ 
    private int slot = 0;
    private boolean incDamage = false;

    public PacketSlopeSelect(){}

    public PacketSlopeSelect(int slot, boolean incDamage) 
    {
        this.slot = slot;
        this.incDamage = incDamage;
    }

    @Override
    public void processData(EntityPlayer entityPlayer, ByteBufInputStream bbis) throws IOException 
    {
        int slot = bbis.readInt();
        boolean incDmg = bbis.readBoolean();
        ItemStack itemStack = entityPlayer.inventory.getStackInSlot(slot);

        // TODO: Implement with slopes
        if (itemStack != null && Block.getBlockFromItem(itemStack.getItem()).equals(BlockRegistry.blockCarpentersSlope)) 
 		  { 
            int maxDmg = BlockCarpentersSlope.slopeType.length - 1;
            int itemDmg = itemStack.getItemDamage();
            itemDmg += incDmg ? 1 : -1;

            if (itemDmg > maxDmg)
            {
                itemDmg = 0;
            } 
            else if (itemDmg < 0) 
            {
                itemDmg = maxDmg;
            }

            itemStack.setItemDamage(itemDmg); 
        } 
    }

    @Override
    public void appendData(PacketBuffer buffer) throws IOException 
    {
        buffer.writeInt(slot);
        buffer.writeBoolean(incDamage);
    } 
}
