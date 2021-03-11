package org.kilocraft.essentials.api.feature;

import net.minecraft.SharedConstants;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.ArrayList;
import java.util.List;

public class ConfigurableFeatures {
    private static final List<ConfigurableFeature> features = new ArrayList<>();
    private static final List<TickListener> tickListeners = new ArrayList<>();
    private static ConfigurableFeatures INSTANCE;

    public ConfigurableFeatures() {
        KiloEssentialsImpl.getLogger().info("Registering the Configurable Features...");
        INSTANCE = this;
    }

    public <F extends ConfigurableFeature> boolean isEnabled(F feature) {
        return features.contains(feature);
    }

    public <F extends ConfigurableFeature> void register(F feature, String configKey) {
        try {
            if (KiloConfig.getMainNode().getNode("features").getNode(configKey).getBoolean()) {
                if (SharedConstants.isDevelopment) {
                    KiloDebugUtils.getLogger().info("Initialing {}", feature.getClass().getName());
                }

                if (feature instanceof TickListener) {
                    tickListeners.add((TickListener) feature);
                }

                features.add(feature);
                feature.register();
            } else features.remove(feature);
        } catch (NullPointerException ignored) {
            //Don't enable the feature:: PASS
        }
    }

    public void loadAll(boolean reload) {
        for (ConfigurableFeature feature : features) {
            if (feature instanceof ReloadableConfigurableFeature) {
                try {
                    ((ReloadableConfigurableFeature) feature).load();
                    ((ReloadableConfigurableFeature) feature).load(reload);
                } catch (Exception e) {
                    KiloEssentials.getLogger().fatal("Can not load the feature " + feature.getClass().getSimpleName(), e);
                }
            }
        }
    }

    public void onTick() {
        for (TickListener listener : tickListeners) {
            try {
                listener.onTick();
            } catch (Exception e) {
                KiloEssentials.getLogger().fatal("An unexpected error occurred while processing a Tick Event", e);
            }
        }
    }

    public static ConfigurableFeatures getInstance() {
        return INSTANCE;
    }
}
