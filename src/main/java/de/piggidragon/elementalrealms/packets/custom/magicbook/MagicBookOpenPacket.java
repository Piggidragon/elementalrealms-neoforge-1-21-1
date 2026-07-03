package de.piggidragon.elementalrealms.packets.custom.magicbook;

import de.piggidragon.elementalrealms.ElementalRealms;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server request to open the affinity book menu.
 */
public record MagicBookOpenPacket() implements CustomPacketPayload {

    public static final Type<MagicBookOpenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "open_magic_book"));

    public static final StreamCodec<ByteBuf, MagicBookOpenPacket> STREAM_CODEC =
            StreamCodec.unit(new MagicBookOpenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
