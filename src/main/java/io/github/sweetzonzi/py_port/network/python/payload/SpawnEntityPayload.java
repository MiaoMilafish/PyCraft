package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

import java.util.concurrent.CompletableFuture;

public record SpawnEntityPayload(
        ResourceLocation level,
        double x,
        double y,
        double z,
        ResourceLocation entity_type
) implements PyPayload {

    public static final Codec<SpawnEntityPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("level").forGetter(SpawnEntityPayload::level),
            Codec.DOUBLE.fieldOf("x").forGetter(SpawnEntityPayload::x),
            Codec.DOUBLE.fieldOf("y").forGetter(SpawnEntityPayload::y),
            Codec.DOUBLE.fieldOf("z").forGetter(SpawnEntityPayload::z),
            ResourceLocation.CODEC.fieldOf("entity_type").forGetter(SpawnEntityPayload::entity_type)
    ).apply(instance, SpawnEntityPayload::new));

    public static final PyPayloadType<SpawnEntityPayload> TYPE =
            new PyPayloadType<>("spawn_entity", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    public static PyHandleResult handle(SpawnEntityPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }

        // 获取维度
        ServerLevel serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, payload.level()));
        if (serverLevel == null) {
            return PyHandleResult.fail("Level " + payload.level() + " not found");
        }

        // 获取实体类型
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(payload.entity_type());
        if (type == null) {
            return PyHandleResult.fail("Invalid entity type: " + payload.entity_type());
        }

        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        // 在主线程执行
        server.execute(() -> {
            try {
                JsonObject result = new JsonObject();

                Entity entity = type.create(serverLevel);
                if (entity == null) {
                    future.completeExceptionally(
                            new RuntimeException("Failed to create entity")
                    );
                    return;
                }

                // 设置位置
                entity.moveTo(payload.x(), payload.y(), payload.z(), 0, 0);

                // 初始化生物
                if (entity instanceof Mob mob) {
                    mob.finalizeSpawn(
                            serverLevel,
                            serverLevel.getCurrentDifficultyAt(entity.blockPosition()),
                            MobSpawnType.COMMAND,
                            null
                    );
                }

                // 添加到世界
                serverLevel.addFreshEntity(entity);

                // 返回信息
                result.addProperty("id", entity.getId());
                result.addProperty("type", payload.entity_type().toString());

                future.complete(result);

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        try {
            JsonObject result = future.get();
            return PyHandleResult.success(result);
        } catch (Exception e) {
            return PyHandleResult.fail("Spawn failed: " + e.getMessage());
        }
    }
}