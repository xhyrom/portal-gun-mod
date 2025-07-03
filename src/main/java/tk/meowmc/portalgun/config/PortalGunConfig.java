package tk.meowmc.portalgun.config;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class PortalGunConfig /*implements ConfigData*/ {
    //@ConfigEntry.Gui.TransitiveObject
    //@ConfigEntry.Category("enabled")
    public final Enabled enabled = new Enabled();
    static final ModConfigSpec commonSpec;
    public static final PortalGunConfig COMMON;
    static {
        final Pair<PortalGunConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(PortalGunConfig::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    private PortalGunConfig(ModConfigSpec.Builder builder) {
        builder.push("enabled");
        builder.pop();
    }

    public static void register() {
        //AutoConfig.register(PortalGunConfig.class, JanksonConfigSerializer::new);
        ModLoadingContext.get().getActiveContainer().registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, commonSpec);
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
