package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record DrawPathPayload(
        List<List<Double>> points,
        int color,
        int duration
) implements PyPayload {
    public static final Codec<DrawPathPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.list(Codec.list(Codec.DOUBLE))
                            .fieldOf("points")
                            .forGetter(DrawPathPayload::points),
                    Codec.INT.fieldOf("color")
                            .forGetter(DrawPathPayload::color),
                    Codec.INT.fieldOf("duration")
                            .forGetter(DrawPathPayload::duration)
            ).apply(instance, DrawPathPayload::new));

    public static final PyPayloadType<DrawPathPayload> TYPE =
            new PyPayloadType<>("draw_path", CODEC);

    @Override
    public PyPayloadType<?> type() {
        return TYPE;
    }

    public static PyHandleResult handle(DrawPathPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server not running");
        }

        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        server.execute(() -> {
            try {
                ServerLevel level = server.overworld(); // 可改为指定维度
                List<List<Double>> pts = payload.points();
                int color = payload.color();
                int duration = payload.duration();
                for (int i = 0; i < pts.size() - 1; i++) {
                    drawLine(level, pts.get(i), pts.get(i + 1), color, duration);
                }
                future.complete(new JsonObject());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        try {
            return PyHandleResult.success(future.get());
        } catch (Exception e) {
            return PyHandleResult.fail(e.getMessage());
        }
    }

    // 插值生成“连续线”
    private static void drawLine(ServerLevel level, List<Double> a, List<Double> b, int color, int duration) {
        double dx = b.get(0) - a.get(0);
        double dy = b.get(1) - a.get(1);
        double dz = b.get(2) - a.get(2);
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(1, (int)(length * 4)); // 防止0

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = a.get(0) + dx * t;
            double y = a.get(1) + dy * t;
            double z = a.get(2) + dz * t;
            DebugPackets.sendGameTestAddMarker(
                    level,
                    new BlockPos((int)x, (int)y, (int)z),
                    "*",
                    color,
                    duration
            );
        }
    }
}