package com.carpentersblocks.util.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.network.ICarpentersPacket;
import com.carpentersblocks.network.PacketSlopeSelect;
import com.carpentersblocks.util.ModLogger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class PacketHandler {

    private final static List<Class> packetCarrier;
    static {
        packetCarrier = new ArrayList<Class>();
        packetCarrier.add(PacketSlopeSelect.class);
    }

    @SubscribeEvent
    public void onServerPacket(ServerCustomPacketEvent event) {
        EntityPlayerMP entityPlayer = ((NetHandlerPlayServer) event.getHandler()).player;
        // Packets arrive on the network thread, so copy the payload and handle it on the server thread
        ByteBuf payload = event.getPacket().payload().copy();
        entityPlayer.getServerWorld().addScheduledTask(() -> {
            try (ByteBufInputStream bbis = new ByteBufInputStream(payload, true)) {
                int packetId = bbis.readInt();
                if (packetId >= 0 && packetId < packetCarrier.size()) {
                    ICarpentersPacket packetClass = (ICarpentersPacket) packetCarrier.get(packetId).newInstance();
                    packetClass.processData(entityPlayer, bbis);
                } else {
                    ModLogger.log(Level.WARN, "Encountered out of range packet Id: " + packetId);
                }
            } catch (Exception e) {
                ModLogger.log(Level.WARN, "Failed to handle packet from " + entityPlayer.getName() + ": " + e);
            }
        });
    }

    public static void sendPacketToServer(ICarpentersPacket packet) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeInt(packetCarrier.indexOf(packet.getClass()));
        try {
            packet.appendData(buffer);
        } catch (IOException e) { }
        CarpentersBlocks.channel.sendToServer(new FMLProxyPacket(new CPacketCustomPayload(CarpentersBlocks.MOD_ID, buffer)));
    }

}
