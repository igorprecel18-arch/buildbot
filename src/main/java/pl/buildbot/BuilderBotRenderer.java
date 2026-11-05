package pl.buildbot;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class BuilderBotRenderer extends BipedEntityRenderer<BuilderBotEntity, BipedEntityModel<BuilderBotEntity>> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");

    public BuilderBotRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(BuilderBotEntity entity) {
        return TEXTURE;
    }
}
