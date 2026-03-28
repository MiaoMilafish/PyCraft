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

public record SpawnEntityPayload(
        ResourceLocation level,      // 维度ID，例如 minecraft:overworld
        double x,                    // 坐标X
        double y,                    // 坐标Y
        double z,                    // 坐标Z
        ResourceLocation entity_type // 实体类型ID，例如 minecraft:pig
) implements PyPayload {

    public static final Codec<SpawnEntityPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("level").forGetter(SpawnEntityPayload::level),
            Codec.DOUBLE.fieldOf("x").forGetter(SpawnEntityPayload::x),
            Codec.DOUBLE.fieldOf("y").forGetter(SpawnEntityPayload::y),
            Codec.DOUBLE.fieldOf("z").forGetter(SpawnEntityPayload::z),
            ResourceLocation.CODEC.fieldOf("entity_type").forGetter(SpawnEntityPayload::entity_type)
    ).apply(instance, SpawnEntityPayload::new));

    public static final PyPayloadType<SpawnEntityPayload> TYPE = new PyPayloadType<>("spawn_entity", CODEC);

    @Override
    public PyPayloadType<?> type() { return TYPE; }

    public static PyHandleResult handle(SpawnEntityPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }
        // 获取目标维度
        ServerLevel serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, payload.level()));
        if (serverLevel == null) {
            return PyHandleResult.fail("Level " + payload.level() + " not found");
        }

        // 获取实体类型
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(payload.entity_type());

        // 在服务器主线程执行生成操作
        server.execute(() -> {
            Entity entity = type.create(serverLevel);
            if (entity != null) {
                entity.moveTo(payload.x(), payload.y(), payload.z(), 0, 0);

                // 如果是生物，初始化其默认行为（如随机花色）
                if (entity instanceof Mob mob) {
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()),
                            MobSpawnType.COMMAND, null);
                }
                serverLevel.addFreshEntity(entity);
            }
        });

        return PyHandleResult.success(new JsonObject());
    }
}