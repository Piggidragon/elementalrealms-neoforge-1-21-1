package de.piggidragon.elementalrealms.client.events;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.client.gui.screens.magicbook.MagicBookButton;
import de.piggidragon.elementalrealms.client.gui.screens.magicbook.MagicBookOverlay;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Adds the affinity book button next to the recipe book on container screens,
 * and renders the affinity book as an overlay that shifts the inventory to the right.
 */
@EventBusSubscriber(modid = ElementalRealms.MODID, value = Dist.CLIENT)
public final class MagicBookButtonHandler {

    private static final Map<AbstractContainerScreen<?>, MagicBookButton> affinityButtons = new HashMap<>();
    private static final Map<AbstractContainerScreen<?>, MagicBookOverlay> affinityOverlays = new HashMap<>();
    private static final Map<AbstractContainerScreen<?>, ImageButton> recipeBookButtons = new HashMap<>();
    private static final int BUTTON_X_OFFSET = 104 + 24;
    private static final int RECIPE_BUTTON_X_OFFSET = 104;
    private static final int BUTTON_Y_OFFSET = 61;
    private static final int MAGIC_BOOK_SHIFT = 77;
    private static final int INVENTORY_WIDTH = 176;
    private static boolean shouldMagicBookBeOpen = false;
    private static Field leftPosField = null;

    private MagicBookButtonHandler() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        RecipeBookComponent recipeBook = null;
        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            recipeBook = inventoryScreen.getRecipeBookComponent();
        }
        if (recipeBook == null) return;

        findAndStoreRecipeBookButton(containerScreen);

        MagicBookOverlay overlay = new MagicBookOverlay(containerScreen.getMinecraft().player);
        affinityOverlays.put(containerScreen, overlay);

        if (shouldMagicBookBeOpen) {
            overlay.setVisible(true);
            if (recipeBook.isVisible()) {
                recipeBook.toggleVisibility();
            }
            shiftInventoryForMagicBook(containerScreen, true);
        }

        MagicBookButton affinityButton = createAffinityButton(containerScreen, recipeBook, overlay);
        affinityButtons.put(containerScreen, affinityButton);
        event.addListener(affinityButton);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        int leftPos = containerScreen.getGuiLeft();
        int topPos = containerScreen.getGuiTop();

        MagicBookButton affinityButton = affinityButtons.get(containerScreen);
        if (affinityButton != null) {
            affinityButton.setPosition(leftPos + BUTTON_X_OFFSET, topPos + BUTTON_Y_OFFSET);
        }

        ImageButton recipeButton = recipeBookButtons.get(containerScreen);
        if (recipeButton != null) {
            recipeButton.setPosition(leftPos + RECIPE_BUTTON_X_OFFSET, topPos + BUTTON_Y_OFFSET);
        }

        if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
            MagicBookOverlay overlay = affinityOverlays.get(containerScreen);
            RecipeBookComponent recipeBook = inventoryScreen.getRecipeBookComponent();
            if (overlay != null && recipeBook != null && overlay.isVisible() && recipeBook.isVisible()) {
                overlay.setVisible(false);
                shouldMagicBookBeOpen = false;
            }
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        MagicBookOverlay overlay = affinityOverlays.get(containerScreen);
        if (overlay == null || !overlay.isVisible()) return;

        int overlayX = containerScreen.getGuiLeft() - overlay.getWidth();
        overlay.render(
                event.getGuiGraphics(),
                overlayX,
                containerScreen.getGuiTop(),
                event.getMouseX(),
                event.getMouseY(),
                event.getPartialTick()
        );
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;
        affinityButtons.remove(containerScreen);
        affinityOverlays.remove(containerScreen);
        recipeBookButtons.remove(containerScreen);
    }

    private static void findAndStoreRecipeBookButton(AbstractContainerScreen<?> containerScreen) {
        for (Renderable renderable : containerScreen.renderables) {
            if (renderable instanceof ImageButton imgButton && !recipeBookButtons.containsValue(imgButton)) {
                recipeBookButtons.put(containerScreen, imgButton);
                ElementalRealms.LOGGER.debug("Found recipe book button at ({}, {})",
                        imgButton.getX(), imgButton.getY());
                return;
            }
        }
    }

    private static @NotNull MagicBookButton createAffinityButton(
            AbstractContainerScreen<?> containerScreen,
            RecipeBookComponent recipeBook,
            MagicBookOverlay overlay
    ) {
        int buttonX = containerScreen.getGuiLeft() + BUTTON_X_OFFSET;
        int buttonY = containerScreen.getGuiTop() + BUTTON_Y_OFFSET;
        return new MagicBookButton(buttonX, buttonY, button -> {
            overlay.toggleVisibility();
            shouldMagicBookBeOpen = overlay.isVisible();
            if (overlay.isVisible() && recipeBook.isVisible()) {
                recipeBook.toggleVisibility();
            }
            shiftInventoryForMagicBook(containerScreen, overlay.isVisible());
        });
    }

    /**
     * Reflectively shifts {@code AbstractContainerScreen.leftPos} so the inventory
     * panel centers next to the affinity book overlay when it's open.
     */
    private static void shiftInventoryForMagicBook(AbstractContainerScreen<?> containerScreen, boolean shift) {
        if (!(containerScreen instanceof EffectRenderingInventoryScreen<?>)) return;

        try {
            if (leftPosField == null) {
                leftPosField = AbstractContainerScreen.class.getDeclaredField("leftPos");
                leftPosField.setAccessible(true);
            }

            int originalLeftPos = (containerScreen.width - INVENTORY_WIDTH) / 2;
            int newLeftPos = originalLeftPos + (shift ? MAGIC_BOOK_SHIFT : 0);
            leftPosField.setInt(containerScreen, newLeftPos);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ElementalRealms.LOGGER.error("Failed to shift inventory for affinity book", e);
        }
    }
}
