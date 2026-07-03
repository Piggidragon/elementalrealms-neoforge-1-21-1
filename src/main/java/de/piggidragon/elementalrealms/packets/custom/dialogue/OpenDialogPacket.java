package de.piggidragon.elementalrealms.packets.custom.dialogue;

import de.piggidragon.elementalrealms.ElementalRealms;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server -> client trigger to open the centered dialog overlay.
 *
 * <p>Carries the entity-type id whose dialog should be shown. The actual
 * dialog content is resolved client-side via
 * {@link de.piggidragon.elementalrealms.registries.attachments.dialogue.DialogRegistry},
 * which keeps the dialog data out of the server wire format.</p>
 */
public record OpenDialogPacket(ResourceLocation entityTypeId) implements CustomPacketPayload {

    public static final Type<OpenDialogPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "open_dialog"));

    public static final StreamCodec<ByteBuf, OpenDialogPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    OpenDialogPacket::entityTypeId,
                    OpenDialogPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
