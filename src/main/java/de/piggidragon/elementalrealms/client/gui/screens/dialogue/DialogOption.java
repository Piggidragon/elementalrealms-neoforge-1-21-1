package de.piggidragon.elementalrealms.client.gui.screens.dialogue;

import net.minecraft.network.chat.Component;

/**
 * One choice inside a {@link DialogStep}. The {@code nextStepId} points at the
 * key of the next step in the same {@link DialogSequence}, or {@code null}
 * to close the overlay.
 *
 * @param label       player-visible option text
 * @param nextStepId  next step in the sequence, or null to end the dialog
 */
public record DialogOption(Component label, String nextStepId) {
}
