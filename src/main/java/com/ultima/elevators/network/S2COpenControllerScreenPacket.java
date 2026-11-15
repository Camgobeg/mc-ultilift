package com.ultima.elevators.network;

import com.ultima.elevators.block.entity.ControllerBlockEntity;
import com.ultima.elevators.client.ClientHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenControllerScreenPacket {
    public final BlockPos pos;
    public final int brandIdx;
    public final int styleIdx;
    public final int speedIdx;
    public final int dwellIdx;
    public final int modeOrdinal;
    public final int floorCount;
    public final int minY;
    public final int maxY;
    public final String statusKey;
    public final String group;
    public final int maxSpeedIdx;

    public S2COpenControllerScreenPacket(BlockPos pos, int brandIdx, int styleIdx, int speedIdx, int dwellIdx,
                                         int modeOrdinal, int floorCount, int minY, int maxY,
                                         String statusKey, String group, int maxSpeedIdx) {
        this.pos = pos;
        this.brandIdx = brandIdx;
        this.styleIdx = styleIdx;
        this.speedIdx = speedIdx;
        this.dwellIdx = dwellIdx;
        this.modeOrdinal = modeOrdinal;
        this.floorCount = floorCount;
        this.minY = minY;
        this.maxY = maxY;
        this.statusKey = statusKey;
        this.group = group;
        this.maxSpeedIdx = maxSpeedIdx;
    }

    public static S2COpenControllerScreenPacket of(ControllerBlockEntity c) {
        BlockPos anchor = c.getAnchorPos();
        int maxIdx = 0;
        float[] speeds = ControllerBlockEntity.SPEEDS;
        for (int i = 0; i < speeds.length; i++) if (speeds[i] <= c.getMode().maxSpeed) maxIdx = i;
        return new S2COpenControllerScreenPacket(
                c.getBlockPos(),
                c.getBrand().ordinal(),
                c.getStyle().ordinal(),
                c.getSpeedIndex(),
                c.getDwellIndex(),
                c.getMode().ordinal(),
                c.getFloorCount(),
                anchor.getY(),
                c.getColumnTopY(),
                c.getStatusKey(),
                c.getGroup(),
                maxIdx);
    }

    public static void encode(S2COpenControllerScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.brandIdx);
        buf.writeVarInt(msg.styleIdx);
        buf.writeVarInt(msg.speedIdx);
        buf.writeVarInt(msg.dwellIdx);
        buf.writeVarInt(msg.modeOrdinal);
        buf.writeVarInt(msg.floorCount);
        buf.writeInt(msg.minY);
        buf.writeInt(msg.maxY);
        buf.writeUtf(msg.statusKey, 96);
        buf.writeUtf(msg.group, 64);
        buf.writeVarInt(msg.maxSpeedIdx);
    }

    public static S2COpenControllerScreenPacket decode(FriendlyByteBuf buf) {
        return new S2COpenControllerScreenPacket(
                buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readInt(), buf.readInt(), buf.readUtf(96), buf.readUtf(64),
                buf.readVarInt());
    }

    public static void handle(S2COpenControllerScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openControllerScreen(msg)));
        ctx.get().setPacketHandled(true);
    }
}
