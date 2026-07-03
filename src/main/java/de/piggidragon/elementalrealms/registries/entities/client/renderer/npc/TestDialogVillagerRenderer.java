package de.piggidragon.elementalrealms.registries.entities.client.renderer.npc;

import de.piggidragon.elementalrealms.registries.entities.custom.npc.TestDialogVillager;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Reuses vanilla's {@link VillagerRenderer} so test NPCs render with the
 * default villager model without us shipping a separate model or texture.
 *
 * <p>The "no custom model" choice is explicit per issue #57 ("Vanilla-Villager-
 * Modell + spaeterer Custom-Texture", Phase 1 vanilla).</p>
 */
public class TestDialogVillagerRenderer extends VillagerRenderer {

    public TestDialogVillagerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // No @Override here on purpose: vanilla's VillagerRenderer uses the
    // generic signature in the parent class; if we override without matching
    // the parent's generic exactly the compiler complains. The vanilla
    // villager base texture is exactly what we want for the test NPC anyway.
    public ResourceLocation getTextureLocation(TestDialogVillager entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
    }
}
