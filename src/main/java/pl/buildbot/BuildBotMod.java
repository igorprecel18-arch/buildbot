package pl.buildbot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BuildBotMod implements ModInitializer {
    public static final String MOD_ID = "buildbot";

    public static final EntityType<BuilderBotEntity> BUILDER_BOT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "builder_bot"),
            EntityType.Builder.create(BuilderBotEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 1.8f)
                    .build("builder_bot"));

    public static final Item SPAWN_EGG = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "builder_bot_spawn_egg"),
            new SpawnEggItem(BUILDER_BOT, 0x3B82F6, 0xFACC15, new Item.Settings()));

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(BUILDER_BOT, BuilderBotEntity.createBuilderAttributes());
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> entries.add(SPAWN_EGG));
    }
}
