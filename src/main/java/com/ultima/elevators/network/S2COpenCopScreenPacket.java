package com.ultima.elevators.network;

import com.ultima.elevators.client.ClientHooks;
import com.ultima.elevators.core.ElevatorBrand;
import com.ultima.elevators.entity.ElevatorCarEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenCopScreenPacket {
    public final int cabId;
    public final int currentFloor;
    public final int floorCount;
    public final int brandIdx;
    public final boolean doorsOpen;

    public S2COpenCopScreenPacket(int cabId, int currentFloor, int floorCount, int brandIdx, boolean doorsOpen) {
        this.cabId = cabId;
        this.currentFloor = currentFloor;
        this.floorCount = floorCount;
        this.brandIdx = brandIdx;
        this.doorsOpen = doorsOpen;
    }

    public static S2COpenCopScreenPacket of(ElevatorCarEntity cab, ServerPlayer player) {
        return new S2COpenCopScreenPacket(
                cab.getId(),
                cab.currentFloorIndex(),
                cab.floorCount(),
                cab.getBrand().ordinal(),
                cab.doorOpenTarget());
    }

    public static void encode(S2COpenCopScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.cabId);
        buf.writeVarInt(msg.currentFloor);
        buf.writeVarInt(msg.floorCount);
        buf.writeVarInt(msg.brandIdx);
        buf.writeBoolean(msg.doorsOpen);
    }

    public static S2COpenCopScreenPacket decode(FriendlyByteBuf buf) {
        return new S2COpenCopScreenPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(S2COpenCopScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openCopScreen(msg)));
        ctx.get().setPacketHandled(true);
    }
}
