package de.piggidragon.elementalrealms.registries.guis.menus;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.registries.guis.menus.custom.MagicBookMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Menu types for mod GUI screens.
 */
public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ElementalRealms.MODID);

    private ModMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }    public static final Supplier<MenuType<MagicBookMenu>> MAGIC_BOOK_MENU =
            MENUS.register("magic_book_menu", () -> IMenuTypeExtension.create(MagicBookMenu::new));


}
