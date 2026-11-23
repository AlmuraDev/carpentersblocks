package com.carpentersblocks.block.data;

import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.util.RotationUtil.Rotation;

import net.minecraft.core.Direction.Axis;

public abstract class RotatableData {
    
    private static final int ROT_BITMASK = 0x3f;
    
    public static void rotate(CbBlockEntity cbTileEntity, Axis axis) {
        int cbMetadata = cbTileEntity.getCbMetadata();
        Rotation rotation = Rotation.get(cbMetadata).getNext(axis);
        setRotation(cbTileEntity, rotation);
    }
    
    public static void resetRotation(CbBlockEntity cbTileEntity) {
        int data = Rotation.X0_Y0_Z0.asInt();
        cbTileEntity.setCbMetadata(data);
    }
    
    public static Rotation getRotation(CbBlockEntity cbTileEntity) {
        return Rotation.get(cbTileEntity.getCbMetadata());
    }
    
    public static void setRotation(CbBlockEntity cbTileEntity, Rotation rotation) {
        int cbMetadata = cbTileEntity.getCbMetadata();
        cbMetadata &= ~ROT_BITMASK;
        cbMetadata |= rotation.asInt();
        cbTileEntity.setCbMetadata(cbMetadata);
    }
    
}
