package de.piggidragon.elementalrealms.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.packets.custom.dialogue.OpenDialogPacket;
import de.piggidragon.elementalrealms.registries.entities.custom.npc.Dialogable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Right-click handler for {@link Dialogable} mobs (issue #57).
 *
 * <p>When the player right-clicks any entity implementing {@link Dialogable}
 * the server sends an {@link OpenDialogPacket} carrying the mob's
 * entity-type id. The client resolves the dialog content via
 * {@code DialogueInit} and opens the {@link
 * de.piggidragon.elementalrealms.client.gui.screens.dialogue.CenteredOverlayScreen}.</p>
 *
 * <p>Vanilla villager right-click behaviour (trade GUI, name-tag, etc.) is
 * suppressed here by returning {@code InteractionResult.SUCCESS}. The trade
 * GUI is intentionally disabled for the dialogable subset because the dialog
 * replaces it.</p>
 */
@EventBusSubscriber(modid = ElementalRealms.MODID)
public final class DialogInteractHandler {

    private DialogInteractHandler() {
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (!(target instanceof Dialogable)) return;
        Player rawPlayer = event.getEntity();
        if (!(rawPlayer instanceof ServerPlayer player)) return;

        ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (entityTypeId == null) {
            ElementalRealms.LOGGER.warn(
                    "DialogInteractHandler: skipping dialog open, entity-type {} not registered",
                    target.getType());
            return;
        }
        player.connection.send(new OpenDialogPacket(entityTypeId));

        // Suppress vanilla villager right-click (trade GUI, name-tag) so the
        // dialog overlay is the only UI the player sees.
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
