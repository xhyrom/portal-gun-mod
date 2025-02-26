package tk.meowmc.portalgun.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class PortalGunConfig /*implements ConfigData*/ {
    //@ConfigEntry.Gui.TransitiveObject
    //@ConfigEntry.Category("enabled")
    public final Enabled enabled = new Enabled();
    static final ForgeConfigSpec commonSpec;
    public static final PortalGunConfig COMMON;
    static {
        final Pair<PortalGunConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(PortalGunConfig::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    private PortalGunConfig(ForgeConfigSpec.Builder builder) {
        builder.push("enabled");
        builder.pop();
    }

    public static void register() {
        //AutoConfig.register(PortalGunConfig.class, JanksonConfigSerializer::new);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonSpec);
    }

    public static PortalGunConfig get() {
        //return AutoConfig.getConfigHolder(PortalGunConfig.class).getConfig();
        return COMMON;
    }

    public static void save() {
        //AutoConfig.getConfigHolder(PortalGunConfig.class).save();
    }


    public static class Enabled {
        //public final boolean enableOldPortalGunModel = false;
        //public final boolean enableRoundPortals = true;
//        public final boolean portalFunneling = true;
    }

}
