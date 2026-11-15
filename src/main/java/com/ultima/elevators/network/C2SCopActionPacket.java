package com.ultima.elevators.network;

import com.ultima.elevators.entity.ElevatorCarEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Car Operating Panel actions. Actions: 0 set destination, 1 hold doors open,
 * 2 close doors now, 3 request open GUI.
 */
public class C2SCopActionPacket {
    public final int cabId;
    public final int action;
    public final int floor;

    public C2SCopActionPacket(int cabId, int action, int floor) {
        this.cabId = cabId;
        this.action = action;
        this.floor = floor;
    }

    public static void encode(C2SCopActionPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.cabId);
        buf.writeVarInt(msg.action);
        buf.writeVarInt(msg.floor);
    }

    public static C2SCopActionPacket decode(FriendlyByteBuf buf) {
        return new C2SCopActionPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(C2SCopActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Level level = player.level();
            if (!(level instanceof ServerLevel sl)) return;
            Entity e = sl.getEntity(msg.cabId);
            if (!(e instanceof ElevatorCarEntity cab)) return;

            boolean aboard = cab.isAboard(player);
            boolean near = player.blockPosition().distSqr(cab.blockPosition()) < 6.0 * 6.0;
            if (!aboard && !near) return;

            switch (msg.action) {
                case 0 -> cab.setPlayerDestination(player.getUUID(), msg.floor);
                case 1 -> cab.holdDoorsOpen();
                case 2 -> cab.closeDoorsNow();
                case 3 -> ModNetwork.sendToPlayer(player, S2COpenCopScreenPacket.of(cab, player));
                default -> { }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
