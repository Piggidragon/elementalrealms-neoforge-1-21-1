package de.piggidragon.elementalrealms.client.gui.screens.dialogue;

import com.mojang.blaze3d.systems.RenderSystem;
import de.piggidragon.elementalrealms.ElementalRealms;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/**
 * Non-modal center-of-screen overlay for in-game dialogs.
 *
 * <p>The screen stays open while the player walks around (no camera lock,
 * no input grab). Esc closes the whole dialog; Enter and 1-9 advance /
 * pick the matching option. Backspace returns to the previous step.</p>
 *
 * <p>The screen is intentionally built on Vanilla's {@link Screen} -
 * Lodestone does not ship a screen-UI base class and is only used for
 * VFX/screen-particles per the {@code elementalrealms-codebase} skill.</p>
 */
public class CenteredOverlayScreen extends Screen {

    @SuppressWarnings("unused")
    private static final ResourceLocation PANEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID,
                    "textures/gui/centered_overlay_panel.png");

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 140;
    private static final int PANEL_ALPHA = 200;
    private static final int RGB_BLACK = 0;
    private static final int PANEL_BORDER = 2;
    private static final int TEXT_PADDING = 12;
    private static final int TITLE_OFFSET_FROM_PANEL_TOP = 14;
    private static final int BODY_OFFSET_FROM_TITLE = 18;
    private static final int OPTION_LINE_HEIGHT = 12;
    private static final int OPTION_HOTKEY_GUTTER = 8;
    private static final int FOOTER_HINT_OFFSET_FROM_PANEL_BOTTOM = 10;
    private static final int HOTKEY_Y_OFFSET_FROM_PANEL_BOTTOM = 22;
    private static final int TEXT_COLOR_TITLE = 0xFFFFFF;
    private static final int TEXT_COLOR_BODY = 0xDCDCDC;
    private static final int TEXT_COLOR_OPTION = 0xFFFF55;
    private static final int TEXT_COLOR_OPTION_NOISE = 0x808080;
    private static final int PANEL_BORDER_COLOR = 0xFF555555;

    private DialogSequence sequence;
    private DialogStep currentStep;
    private String previousStepId;

    /**
     * Public constructor so future NPC implementations can construct the
     * screen directly. The {@code sequence}'s first step is shown.
     */
    public CenteredOverlayScreen(DialogSequence sequence) {
        super(Component.empty());
        this.sequence = sequence;
        this.currentStep = sequence.first();
    }

    /**
     * Replace the currently shown step. The packet-handler uses this for the
     * NPC-switch path: if a player right-clicks a different dialogable mob
     * while this screen is still open, the latest packet overwrites the
     * current step instead of stacking a second overlay.
     */
    public void swapTo(DialogSequence other, DialogStep start) {
        this.sequence = other;
        this.currentStep = start;
        this.previousStepId = null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Esc closes the screen entirely.
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        // Enter / Space advance (sneak is handled client-side as shift-down,
        // not as a single-key press, so we don't poll for it here).
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER
                || keyCode == GLFW.GLFW_KEY_SPACE) {
            advance();
            return true;
        }

        // Backspace returns to the previous step if there is one.
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && previousStepId != null) {
            sequence.step(previousStepId).ifPresent(prev -> {
                currentStep = prev;
                previousStepId = null;
            });
            return true;
        }

        // 1-9 pick the matching option (index 0-8).
        for (int i = 0; i < currentStep.options().size() && i < 9; i++) {
            int hotkey = GLFW.GLFW_KEY_1 + i;
            if (keyCode == hotkey) {
                pickOption(i);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void pickOption(int index) {
        if (index < 0 || index >= currentStep.options().size()) return;
        DialogOption option = currentStep.options().get(index);
        if (option.nextStepId() == null) {
            this.onClose();
            return;
        }
        sequence.step(option.nextStepId()).ifPresent(next -> {
            previousStepId = currentStep.id();
            currentStep = next;
        });
    }

    private void advance() {
        // No options -> Enter closes the dialog. The original spec ("Sneak/Enter
        // ueberspringt Text") treats a no-option step as a single-message step
        // and Enter drops out of it.
        if (currentStep.options().isEmpty()) {
            this.onClose();
            return;
        }
        // Multi-choice: Enter defaults to the highlighted first option.
        pickOption(0);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Non-modal: do NOT call renderDirtBackground / vignette - we don't
        // want the dark screen tint or pause behaviour.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        super.render(graphics, mouseX, mouseY, partialTick);

        int screenW = this.minecraft.getWindow().getGuiScaledWidth();
        int screenH = this.minecraft.getWindow().getGuiScaledHeight();
        int x0 = (screenW - PANEL_WIDTH) / 2;
        int y0 = (screenH - PANEL_HEIGHT) / 2;

        // Translucent panel (alpha=200, RGB=0).
        graphics.fill(x0, y0, x0 + PANEL_WIDTH, y0 + PANEL_HEIGHT,
                (PANEL_ALPHA << 24) | (RGB_BLACK << 16) | (RGB_BLACK << 8) | RGB_BLACK);
        // Border.
        graphics.fill(x0, y0, x0 + PANEL_WIDTH, y0 + PANEL_BORDER, PANEL_BORDER_COLOR);
        graphics.fill(x0, y0 + PANEL_HEIGHT - PANEL_BORDER,
                x0 + PANEL_WIDTH, y0 + PANEL_HEIGHT, PANEL_BORDER_COLOR);
        graphics.fill(x0, y0, x0 + PANEL_BORDER, y0 + PANEL_HEIGHT, PANEL_BORDER_COLOR);
        graphics.fill(x0 + PANEL_WIDTH - PANEL_BORDER, y0,
                x0 + PANEL_WIDTH, y0 + PANEL_HEIGHT, PANEL_BORDER_COLOR);

        // Title.
        Component title = currentStep.title();
        int titleX = x0 + TEXT_PADDING;
        int titleY = y0 + TITLE_OFFSET_FROM_PANEL_TOP;
        graphics.drawString(this.font, title, titleX, titleY, TEXT_COLOR_TITLE, false);

        // Body - limit the rendered width so text wraps inside the panel.
        Component body = currentStep.body();
        int bodyX = x0 + TEXT_PADDING;
        int bodyY = titleY + BODY_OFFSET_FROM_TITLE;
        int bodyW = PANEL_WIDTH - 2 * TEXT_PADDING;
        for (var line : this.font.split(body, bodyW)) {
            graphics.drawString(this.font, line, bodyX, bodyY, TEXT_COLOR_BODY, false);
            bodyY += this.font.lineHeight;
        }

        // Options with hotkeys (1-9). Each option is drawn on its own line.
        int optY = y0 + PANEL_HEIGHT - HOTKEY_Y_OFFSET_FROM_PANEL_BOTTOM
                - currentStep.options().size() * OPTION_LINE_HEIGHT;
        for (int i = 0; i < currentStep.options().size() && i < 9; i++) {
            DialogOption option = currentStep.options().get(i);
            int lineY = optY + i * OPTION_LINE_HEIGHT;
            String hotkey = "[" + (i + 1) + "] ";
            graphics.drawString(this.font, hotkey, bodyX, lineY, TEXT_COLOR_OPTION_NOISE, false);
            graphics.drawString(this.font, option.label(),
                    bodyX + this.font.width(hotkey) + OPTION_HOTKEY_GUTTER, lineY,
                    TEXT_COLOR_OPTION, false);
        }

        // Footer hint.
        String hint = "Esc schliessen  -  Enter weiter  -  1-9 waehlen  -  Backspace zurueck";
        int hintW = this.font.width(hint);
        graphics.drawString(this.font, hint,
                x0 + (PANEL_WIDTH - hintW) / 2,
                y0 + PANEL_HEIGHT - FOOTER_HINT_OFFSET_FROM_PANEL_BOTTOM,
                TEXT_COLOR_OPTION_NOISE, false);

        RenderSystem.disableBlend();
    }
}
