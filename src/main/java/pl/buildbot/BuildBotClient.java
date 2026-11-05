package pl.buildbot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class BuildBotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(BuildBotMod.BUILDER_BOT, BuilderBotRenderer::new);
    }
}
