package com.ultima.elevators.network;

import com.ultima.elevators.block.entity.ControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Controller configuration GUI actions.
 * Actions: 0 open, 1 brand+1, 2 brand-1, 3 style+1, 4 style-1, 5 speed+1, 6 speed-1,
 *          7 dwell+1, 8 dwell-1, 9 set group (text), 10 recalibrate.
 */
public class C2SControllerActionPacket {
    public final BlockPos pos;
    public final int action;
    public final int value;
    public final String text;

    public C2SControllerActionPacket(BlockPos pos, int action, int value, String text) {
        this.pos = pos;
        this.action = action;
        this.value = value;
        this.text = text == null ? "" : text;
    }

    public static void encode(C2SControllerActionPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.action);
        buf.writeVarInt(msg.value);
        buf.writeUtf(msg.text, 64);
    }

    public static C2SControllerActionPacket decode(FriendlyByteBuf buf) {
        return new C2SControllerActionPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(64));
    }

    public static void handle(C2SControllerActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Level level = player.level();
            if (player.blockPosition().distSqr(msg.pos) > 128.0 * 128.0) return;
            if (!(level.getBlockEntity(msg.pos) instanceof ControllerBlockEntity c)) return;

            switch (msg.action) {
                case 0 -> ModNetwork.sendToPlayer(player, S2COpenControllerScreenPacket.of(c));
                case 1 -> c.cycleBrand(1);
                case 2 -> c.cycleBrand(-1);
                case 3 -> c.cycleStyle(1);
                case 4 -> c.cycleStyle(-1);
                case 5 -> c.cycleSpeed(1);
                case 6 -> c.cycleSpeed(-1);
                case 7 -> c.cycleDwell(1);
                case 8 -> c.cycleDwell(-1);
                case 9 -> c.setGroup(msg.text);
                case 10 -> c.markDirty();
                default -> { }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
