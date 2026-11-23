package com.carpentersblocks.network;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.item.ItemBlockCarpentersSlope;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sneak and scroll with a slope in hand switches the slope it places (1.12.2's PacketSlopeSelect).
 *
 * @param slot the held hotbar slot, so a scroll can't change another item
 * @param forward whether to switch to the next slope (else the previous one)
 */
public record SlopeSelectPayload(int slot, boolean forward) implements CustomPacketPayload {

    public static final Type<SlopeSelectPayload> TYPE = new Type<>(CarpentersBlocks.id("slope_select"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlopeSelectPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SlopeSelectPayload::slot,
        ByteBufCodecs.BOOL, SlopeSelectPayload::forward,
        SlopeSelectPayload::new);

    @Override
    public Type<SlopeSelectPayload> type() {
        return TYPE;
    }

    /**
     * Switches the held slope (server).
     */
    public static void handle(SlopeSelectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (payload.slot() != player.getInventory().getSelectedSlot()) {
                return;
            }
            ItemStack itemStack = player.getInventory().getItem(payload.slot());
            if (itemStack.getItem() instanceof ItemBlockCarpentersSlope) {
                ItemBlockCarpentersSlope.cycle(itemStack, payload.forward());
            }
        });
    }

}
