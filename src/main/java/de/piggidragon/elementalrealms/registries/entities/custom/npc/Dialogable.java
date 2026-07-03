package de.piggidragon.elementalrealms.registries.entities.custom.npc;

/**
 * Marker interface: any entity that implements this can be right-clicked
 * to open the centered dialog overlay (issue #57).
 *
 * <p>Kept as a pure marker on purpose - no methods here, no state. The
 * dialog content is dispatched by the entity-type id
 * ({@link de.piggidragon.elementalrealms.registries.attachments.dialogue.DialogRegistry}),
 * not by an instance method, so the dialog factory can pull live data
 * (player, level, time-of-day, ...) on demand.</p>
 *
 * <p>Today's implementors: {@link TestDialogVillager}. Future implementors
 * come from issue #62 (Headmaster), #63 (Lehrmeister) and #66 (Ambient /
 * Trading / Secret) - all of which will live in
 * {@code registries/entities/custom/npc}.</p>
 */
public sealed interface Dialogable permits TestDialogVillager {
}
