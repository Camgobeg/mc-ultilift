package com.ultima.elevators.network;

import com.ultima.elevators.block.entity.KioskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SKioskSelectPacket {
    public final BlockPos pos;
    public final int destFloor;

    public C2SKioskSelectPacket(BlockPos pos, int destFloor) {
        this.pos = pos;
        this.destFloor = destFloor;
    }

    public static void encode(C2SKioskSelectPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.destFloor);
    }

    public static C2SKioskSelectPacket decode(FriendlyByteBuf buf) {
        return new C2SKioskSelectPacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(C2SKioskSelectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Level level = player.level();
            if (!(level instanceof ServerLevel)) return;
            if (player.blockPosition().distSqr(msg.pos) > 32.0 * 32.0) return;
            if (level.getBlockEntity(msg.pos) instanceof KioskBlockEntity kiosk) {
                kiosk.assignDestination(player, msg.destFloor);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
