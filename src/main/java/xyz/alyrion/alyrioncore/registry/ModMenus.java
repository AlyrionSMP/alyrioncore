package xyz.alyrion.alyrioncore.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.menu.CrateMenu;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AlyrionCore.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CrateMenu>> CRATE =
            MENUS.register("crate", () -> IMenuTypeExtension.create(CrateMenu::new));
}
