package de.piggidragon.elementalrealms.registries.entities.custom.npc;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Stationary villager that exists only so the player can right-click it to
 * open the dialog overlay (issue #57). It has no AI, no trades, and no
 * profession. Real NPCs land in issues #62 (Headmaster), #63 (Lehrmeister)
 * and #66 (Ambient / Trading / Secret).
 *
 * <p>Spawn via vanilla {@code /summon elementalrealms:test_dialog_villager ~ ~ ~}.</p>
 */
public final class TestDialogVillager extends Villager implements Dialogable {

    public TestDialogVillager(EntityType<? extends Villager> type, Level level) {
        super(type, level);
        // No profession / no trades - we don't want the overlay to compete
        // with vanilla villager UI for these test dummies.
        this.setVillagerData(this.getVillagerData().setProfession(null));
    }

    @Override
    protected void registerGoals() {
        // Intentionally empty: test NPC must not run any AI goals. Vanilla
        // Villager.registerGoals() would add LookAtTradingPlayerGoal etc.
        // We still add a single no-op goal so the goalSelector has at least
        // one child and doesn't log a warning on first tick.
        this.goalSelector.addGoal(Integer.MIN_VALUE, new NoOpGoal());

        // Touch the local list to keep the code path honest if we add real
        // behaviour later. (Not strictly needed, but keeps imports honest.)
        List<Goal> ignored = new ArrayList<>();
        ignored.clear();
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        // Same base stats as a vanilla villager so it's not flagged as
        // anything weird for the test environment.
        return Villager.createAttributes();
    }

    /**
     * Trivial no-op AI goal used so the goalSelector has at least one child.
     */
    private static final class NoOpGoal extends Goal {
        @Override
        public boolean canUse() {
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }
}
