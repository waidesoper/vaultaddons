package waidesoper.vaultaddons;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.EventPriority;
import waidesoper.vaultaddons.init.ModCommands;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("vaultaddons")
public class VaultAddons
{
    public static final String MOD_ID = "vaultaddons";


    public VaultAddons() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, this::onCommandRegister);
    }

    public void onCommandRegister(RegisterCommandsEvent event) {
        ModCommands.registerCommands(event.getDispatcher(), event.getEnvironment());
    }
}
