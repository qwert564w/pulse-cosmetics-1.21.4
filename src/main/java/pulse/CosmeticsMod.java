package pulse;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pulse.cosmetic.LocalCosmetics;

public class CosmeticsMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("cosmetics");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Cosmetics 1.21.4 port loaded, entries={}", LocalCosmetics.size());
        // auto-equip first non-cape so something is visible without GUI
        LocalCosmetics.selectDefault();
        LOGGER.info("Default cosmetic selected, active={}", LocalCosmetics.selectedIndices());
    }
}
