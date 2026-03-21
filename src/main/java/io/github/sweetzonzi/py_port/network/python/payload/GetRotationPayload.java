package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public record GetRotationPayload() implements PyPayload {
    public static final Codec<GetRotationPayload> CODEC =
            Codec.unit(new GetRotationPayload());

    public static final PyPayloadType<GetRotationPayload> TYPE =
            new PyPayloadType<>("get_rotation", CODEC);

    @Override
    public PyPayloadType<?> type() {
        return TYPE;
    }

    public static PyHandleResult handle(GetRotationPayload payload, PyContext context) {

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return PyHandleResult.fail("Player not available");
        }

        LocalPlayer player = mc.player;

        float yaw = player.getYRot();
        float pitch = player.getXRot();

        JsonObject data = new JsonObject();
        data.addProperty("yaw", yaw);
        data.addProperty("pitch", pitch);

        return PyHandleResult.success(data);
    }
}