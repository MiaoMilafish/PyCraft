package io.github.sweetzonzi.py_port.network.java.payload;

import io.github.sweetzonzi.py_port.PyCraft;
import io.github.sweetzonzi.py_port.common.agent.AbstractAgent;
import io.github.sweetzonzi.py_port.common.agent.AgentManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AgentSyncPayload(
        int id,
        List<SynchedEntityData.DataValue<?>> syncData //发生变化的数据
) implements CustomPacketPayload {
    public static final Type<AgentSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PyCraft.MOD_ID, "agent_data_sync_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AgentSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull AgentSyncPayload decode(RegistryFriendlyByteBuf buffer) {
            int id = buffer.readInt();
            List<SynchedEntityData.DataValue<?>> syncData = new ArrayList<>();
            int i;
            while ((i = buffer.readUnsignedByte()) != 255) {
                syncData.add(SynchedEntityData.DataValue.read(buffer, i));
            }
            return new AgentSyncPayload(id, syncData);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, AgentSyncPayload value) {
            buffer.writeInt(value.id());
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

    public static void handler(final AgentSyncPayload payload, final IPayloadContext context) {
        AbstractAgent agent = AgentManager.getAgent(context.player().level(), payload.id());
        if (agent instanceof AbstractAgent) {
            context.enqueueWork(() -> agent.getSyncedData().assignValues(payload.syncData()));
        } else
            PyCraft.LOGGER.error("维度{}收到不存在智能体的同步数据包: {}", context.player().level().dimension().location(), payload.id);
    }
}
