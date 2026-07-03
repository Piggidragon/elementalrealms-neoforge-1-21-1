package de.piggidragon.elementalrealms.client.gui.screens.dialogue;

import de.piggidragon.elementalrealms.packets.custom.dialogue.OpenDialogPacket;
import de.piggidragon.elementalrealms.registries.entities.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Client-side glue: opens {@link CenteredOverlayScreen} from a packet, and
 * handles NPC-switch by replacing the currently open overlay instead of
 * stacking a second one.
 */
public final class DialogueClient {

    private DialogueClient() {
    }

    /**
     * Called by the packet handler with every incoming {@link OpenDialogPacket}.
     * If a {@link CenteredOverlayScreen} is already open it gets swapped to the
     * new dialog; otherwise a fresh overlay is opened.
     */
    public static void openDialog(OpenDialogPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Screen current = mc.screen;
        String entityTypeId = packet.entityTypeId().toString();

        DialogSequence sequence = DialogueInit.build(entityTypeId, current);
        if (sequence == null) {
            // Unknown entity-type - fall back to the test NPC's sequence so
            // the player still gets visible feedback instead of silent no-op.
            String fallbackId = BuiltInRegistries.ENTITY_TYPE.getKey(
                    ModEntities.TEST_DIALOG_VILLAGER.get()).toString();
            sequence = DialogueInit.build(fallbackId, current);
        }
        if (sequence == null) return;

        if (current instanceof CenteredOverlayScreen overlay) {
            overlay.swapTo(sequence, sequence.first());
            return;
        }
        mc.setScreen(new CenteredOverlayScreen(sequence));
    }
}
