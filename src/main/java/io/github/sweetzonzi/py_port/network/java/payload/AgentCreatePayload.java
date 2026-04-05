package io.github.sweetzonzi.py_port.network.java.payload;

import io.github.sweetzonzi.py_port.PyCraft;
import io.github.sweetzonzi.py_port.common.agent.AbstractAgent;
import io.github.sweetzonzi.py_port.common.agent.AgentManager;
import io.github.sweetzonzi.py_port.common.agent.AgentRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AgentCreatePayload(
        int id,
        String agentType,
        List<SynchedEntityData.DataValue<?>> syncData
) implements CustomPacketPayload {
    public static final Type<AgentCreatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PyCraft.MOD_ID, "agent_create_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AgentCreatePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull AgentCreatePayload decode(RegistryFriendlyByteBuf buffer) {
            int id = buffer.readInt();
            String agentType = buffer.readUtf();
            List<SynchedEntityData.DataValue<?>> syncData = new ArrayList<>();
            int i;
            while ((i = buffer.readUnsignedByte()) != 255) {
                syncData.add(SynchedEntityData.DataValue.read(buffer, i));
            }
            return new AgentCreatePayload(id, agentType, syncData);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, AgentCreatePayload value) {
            buffer.writeInt(value.id());
            buffer.writeUtf(value.agentType());
            for (SynchedEntityData.DataValue<?> datavalue : value.syncData()) {
                datavalue.write(buffer);
            }
            buffer.writeByte(255);
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handler(final AgentCreatePayload payload, final IPayloadContext context) {
        try {
            Level level = context.player().level();
            AbstractAgent agent = AgentRegistry.create(payload.agentType(), level);
            agent.setId(payload.id());
            context.enqueueWork(() -> {
                agent.getSyncedData().assignValues(payload.syncData());
                agent.addToLevel();
            });
        } catch (IllegalArgumentException e) {
            PyCraft.LOGGER.error("Failed to create agent of type {}", payload.agentType(), e);
        }
    }
}
