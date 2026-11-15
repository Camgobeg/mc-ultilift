package com.ultima.elevators.network;

import com.ultima.elevators.UltimaElevators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModNetwork {
    private ModNetwork() {}

    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(UltimaElevators.MODID, "main"),
            () -> VERSION,
            s -> true,
            s -> true);

    private static int id = 0;

    public static void init() {
        CHANNEL.registerMessage(id++, C2SControllerActionPacket.class,
                C2SControllerActionPacket::encode, C2SControllerActionPacket::decode,
                C2SControllerActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SKioskSelectPacket.class,
                C2SKioskSelectPacket::encode, C2SKioskSelectPacket::decode,
                C2SKioskSelectPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SCopActionPacket.class,
                C2SCopActionPacket::encode, C2SCopActionPacket::decode,
                C2SCopActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, S2COpenControllerScreenPacket.class,
                S2COpenControllerScreenPacket::encode, S2COpenControllerScreenPacket::decode,
                S2COpenControllerScreenPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, S2COpenKioskScreenPacket.class,
                S2COpenKioskScreenPacket::encode, S2COpenKioskScreenPacket::decode,
                S2COpenKioskScreenPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, S2COpenCopScreenPacket.class,
                S2COpenCopScreenPacket::encode, S2COpenCopScreenPacket::decode,
                S2COpenCopScreenPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToPlayer(ServerPlayer player, Object msg) {
        if (player != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
        }
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }
}
