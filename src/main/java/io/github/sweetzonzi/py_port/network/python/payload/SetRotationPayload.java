package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public record SetRotationPayload(
        float yaw,
        float pitch
) implements PyPayload {

    public static final Codec<SetRotationPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("yaw").forGetter(SetRotationPayload::yaw),
                    Codec.FLOAT.fieldOf("pitch").forGetter(SetRotationPayload::pitch)
            ).apply(instance, SetRotationPayload::new));

    public static final PyPayloadType<SetRotationPayload> TYPE =
            new PyPayloadType<>("set_rotation", CODEC);

    @Override
    public PyPayloadType<?> type() {
        return TYPE;
    }

    public static PyHandleResult handle(SetRotationPayload payload, PyContext context) {

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return PyHandleResult.fail("Player not available");
        }

        mc.execute(() -> {
            LocalPlayer player = mc.player;

            // 设置视角
            player.setYRot(payload.yaw());
            player.setXRot(payload.pitch());

            // 同步头部（避免身体和视角分离）
            player.yHeadRot = payload.yaw();
            player.yBodyRot = payload.yaw();
        });

        JsonObject data = new JsonObject();
        data.addProperty("status", "success");

        return PyHandleResult.success(data);
    }
}