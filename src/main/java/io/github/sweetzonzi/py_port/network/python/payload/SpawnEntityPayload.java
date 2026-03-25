package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public record SpawnEntityPayload(
        String entity_type,
        double x,
        double y,
        double z
) implements PyPayload {

    public static final Codec<SpawnEntityPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("entity_type").forGetter(SpawnEntityPayload::entity_type),
                    Codec.DOUBLE.fieldOf("x").forGetter(SpawnEntityPayload::x),
                    Codec.DOUBLE.fieldOf("y").forGetter(SpawnEntityPayload::y),
                    Codec.DOUBLE.fieldOf("z").forGetter(SpawnEntityPayload::z)
            ).apply(instance, SpawnEntityPayload::new));

    public static final PyPayloadType<SpawnEntityPayload> TYPE =
            new PyPayloadType<>("spawn_entity", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    public static PyHandleResult handle(SpawnEntityPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server not running");
        }

        ServerLevel level = server.overworld();

        EntityType<?> type = EntityType.byString(payload.entity_type()).orElse(null);
        if (type == null) {
            return PyHandleResult.fail("Invalid entity type");
        }

        Entity entity = type.create(level);
        if (entity == null) {
            return PyHandleResult.fail("Failed to create entity");
        }

        entity.moveTo(payload.x(), payload.y(), payload.z(), 0, 0);
        level.addFreshEntity(entity);

        JsonObject result = new JsonObject();
        result.addProperty("entity_id", entity.getId());

        return PyHandleResult.success(result);
    }
}