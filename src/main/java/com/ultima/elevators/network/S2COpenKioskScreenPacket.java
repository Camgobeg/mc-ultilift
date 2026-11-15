package com.ultima.elevators.network;

import com.ultima.elevators.client.ClientHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenKioskScreenPacket {
    public final BlockPos pos;
    public final int floorCount;
    public final String group;

    public S2COpenKioskScreenPacket(BlockPos pos, int floorCount, String group) {
        this.pos = pos;
        this.floorCount = floorCount;
        this.group = group;
    }

    public static void encode(S2COpenKioskScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.floorCount);
        buf.writeUtf(msg.group, 64);
    }

    public static S2COpenKioskScreenPacket decode(FriendlyByteBuf buf) {
        return new S2COpenKioskScreenPacket(buf.readBlockPos(), buf.readVarInt(), buf.readUtf(64));
    }

    public static void handle(S2COpenKioskScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openKioskScreen(msg)));
        ctx.get().setPacketHandled(true);
    }
}
