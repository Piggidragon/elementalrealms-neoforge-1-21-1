package de.piggidragon.elementalrealms.client.gui.screens.dialogue;

import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central wiring for dialog factories. Each registered id maps to a
 * factory that builds the {@link DialogSequence} for that entity-type.
 *
 * <p>The actual dialog content is intentionally tiny - this issue (#57) only
 * delivers the system, not the dialogs themselves. Real dialogs land in
 * issue #62 (Headmaster), #63 (Lehrmeister), #64 (Lectures), #65 (Library
 * Bookshelf) and #66 (Ambient / Trading / Secret).</p>
 */
public final class DialogueInit {

    /**
     * Functional factory for building the dialog sequence. The {@code opener}
     * is the screen the overlay replaces (or {@code null} when opening fresh).
     */
    @FunctionalInterface
    public interface SequenceFactory {
        DialogSequence build(Screen opener);
    }

    private static final Map<String, SequenceFactory> FACTORIES = new LinkedHashMap<>();

    private DialogueInit() {
    }

    /**
     * Called from {@code ElementalRealmsClient} at client setup. Safe to call
     * repeatedly: registering the same id overwrites the previous factory.
     */
    public static void registerDefaults() {
        register(testDialogVillagerId(), DialogueInit::buildTestSequence);

        ElementalRealms.LOGGER.info("DialogueInit: registered {} dialog factory(ies).",
                FACTORIES.size());
    }

    /** Register a dialog factory for the given entity-type id. */
    public static void register(String entityTypeId, SequenceFactory factory) {
        FACTORIES.put(entityTypeId, factory);
    }

    /** Resolves the sequence for the given entity-type id, or null if unknown. */
    public static DialogSequence build(String entityTypeId, Screen opener) {
        SequenceFactory factory = FACTORIES.get(entityTypeId);
        if (factory == null) return null;
        return factory.build(opener);
    }

    /** All registered factories. Used by tests. */
    public static Map<String, SequenceFactory> factories() {
        return Collections.unmodifiableMap(FACTORIES);
    }

    private static String testDialogVillagerId() {
        return ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "test_dialog_villager")
                .toString();
    }

    /**
     * Dummy 2-step sequence used by the in-game test NPC to verify the
     * overlay system end-to-end.
     */
    private static DialogSequence buildTestSequence(@SuppressWarnings("unused") Screen opener) {
        return new DialogSequence("welcome")
                .add(new DialogStep(
                        "welcome",
                        Component.translatable("dialog.elementalrealms.test_npc.title"),
                        Component.translatable("dialog.elementalrealms.test_npc.welcome.body"),
                        java.util.List.of(
                                new DialogOption(
                                        Component.translatable(
                                                "dialog.elementalrealms.test_npc.option.greet"),
                                        "greet"),
                                new DialogOption(
                                        Component.translatable(
                                                "dialog.elementalrealms.test_npc.option.bye"),
                                        null))))
                .add(new DialogStep(
                        "greet",
                        Component.translatable("dialog.elementalrealms.test_npc.title"),
                        Component.translatable("dialog.elementalrealms.test_npc.greet.body"),
                        java.util.List.of(new DialogOption(
                                Component.translatable(
                                        "dialog.elementalrealms.test_npc.option.bye"),
                                null))));
    }
}
