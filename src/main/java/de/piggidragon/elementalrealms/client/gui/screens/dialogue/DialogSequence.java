package de.piggidragon.elementalrealms.client.gui.screens.dialogue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Ordered multi-step dialog identified by its first step id.
 *
 * <p>A sequence is built by adding steps in display order. The map preserves
 * insertion order so {@link #steps()} reflects authoring order.</p>
 */
public final class DialogSequence {

    private final String firstStepId;
    private final Map<String, DialogStep> steps = new LinkedHashMap<>();

    public DialogSequence(String firstStepId) {
        this.firstStepId = firstStepId;
    }

    /**
     * Append a step. The first call defines the starting step (its id must
     * equal {@code firstStepId}); later calls may be in any order.
     */
    public DialogSequence add(DialogStep step) {
        if (steps.isEmpty() && !step.id().equals(firstStepId)) {
            throw new IllegalArgumentException(
                    "First step id must be '" + firstStepId + "', got '" + step.id() + "'");
        }
        steps.put(step.id(), step);
        return this;
    }

    public String firstStepId() {
        return firstStepId;
    }

    public DialogStep first() {
        return step(firstStepId).orElseThrow(() ->
                new IllegalStateException("DialogSequence has no steps"));
    }

    public Optional<DialogStep> step(String id) {
        return Optional.ofNullable(steps.get(id));
    }

    /** Read-only steps in authoring order. Used for diagnostics. */
    public Map<String, DialogStep> steps() {
        return Collections.unmodifiableMap(steps);
    }
}
