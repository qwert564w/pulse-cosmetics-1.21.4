package pulse.cosmetic;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
// no RenderLayers
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ru.pulse.cosmetic.model.CosmeticModel;
import ru.pulse.cosmetic.render.CosmeticRenderer;

/**
 * 1.21.4 port: uses VertexConsumerProvider (not OrderedRenderCommandQueue).
 */
public class CosmeticFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    public CosmeticFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        MinecraftClient client = MinecraftClient.getInstance();
        AbstractClientPlayerEntity player = client.player;
        if (player == null || vertexConsumers == null || state == null) {
            return;
        }
        // only local player cosmetics
        if (state.spectator) {
            return;
        }
        if (state.id != player.getId()) {
            return;
        }

        List<Integer> selected = LocalCosmetics.selectedIndices();
        if (selected.isEmpty()) {
            return;
        }

        PlayerEntityModel model = this.getContextModel();
        for (Integer selectedIndex : selected) {
            if (selectedIndex == null || "cape".equals(LocalCosmetics.type(selectedIndex))) {
                continue;
            }
            CosmeticModel cosmetic = LocalCosmetics.modelFor(selectedIndex);
            if (cosmetic == null || cosmetic.getTextureId() == null) {
                continue;
            }
            Identifier tex = cosmetic.getTextureId();
            RenderLayer layer = RenderLayer.getEntityCutoutNoCull(tex);
            VertexConsumer vc = vertexConsumers.getBuffer(layer);
            matrices.push();
            try {
                CosmeticRenderer.getInstance().renderCosmetic(
                    cosmetic, player, matrices, vc, light, model, limbDistance
                );
            } catch (Throwable t) {
                // swallow to avoid crashing the whole render pipeline
            }
            matrices.pop();
        }
    }
}
