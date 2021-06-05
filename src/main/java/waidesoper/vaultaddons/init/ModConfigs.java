package waidesoper.vaultaddons.init;

import waidesoper.vaultaddons.config.*;

public class ModConfigs {
    public static VaultCoopOnlyConfig VAULT_COOP_ONLY;

    public static void register() {
        VAULT_COOP_ONLY = (VaultCoopOnlyConfig) new VaultCoopOnlyConfig().readConfig();
    }
}
