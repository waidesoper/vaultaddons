package waidesoper.vaultaddons.config;

import com.google.gson.annotations.Expose;

public class VaultCoopOnlyConfig extends iskallia.vault.config.Config{
    @Expose
    public Boolean IS_COOP_ONLY;
    
    public String getName() {
        return "vault_coop_only";
    }

    public void reset(){
        this.IS_COOP_ONLY=false;
    }
}
