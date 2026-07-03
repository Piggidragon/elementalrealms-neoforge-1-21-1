package de.piggidragon.elementalrealms.client.gui.screens.dialogue;

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * One screen-state of a {@link DialogSequence}: a title, body text, and an
 * ordered set of choices. The list may be empty for a pure text/skip step.
 *
 * @param id      stable identifier inside its sequence (used by options)
 * @param title   header text shown above the body
 * @param body    paragraph body
 * @param options selectable choices, ordered by hotkey (1-9)
 */
public record DialogStep(String id, Component title, Component body, List<DialogOption> options) {

    public DialogStep {
        options = options == null ? List.of() : List.copyOf(options);
    }
}
