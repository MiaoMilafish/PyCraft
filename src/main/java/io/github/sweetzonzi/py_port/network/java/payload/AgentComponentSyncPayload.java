package io.github.sweetzonzi.py_port.network.java.payload;

import io.github.sweetzonzi.py_port.PyCraft;
import io.github.sweetzonzi.py_port.common.agent.AbstractAgent;
import io.github.sweetzonzi.py_port.common.agent.AgentManager;
import io.github.sweetzonzi.py_port.common.agent.component.AbstractAgentComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AgentComponentSyncPayload(
        int id,
        String component,
        List<SynchedEntityData.DataValue<?>> syncData //发生变化的数据
) implements CustomPacketPayload {
    public static final Type<AgentComponentSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PyCraft.MOD_ID, "agent_component_sync_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AgentComponentSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull AgentComponentSyncPayload decode(RegistryFriendlyByteBuf buffer) {
            int id = buffer.readInt();
            String component = buffer.readUtf();
            List<SynchedEntityData.DataValue<?>> syncData = new ArrayList<>();
            int i;
            while ((i = buffer.readUnsignedByte()) != 255) {
                syncData.add(SynchedEntityData.DataValue.read(buffer, i));
            }
            return new AgentComponentSyncPayload(id, component, syncData);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, AgentComponentSyncPayload value) {
            buffer.writeInt(value.id());
            buffer.writeUtf(value.component());
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

    public static void handler(final AgentComponentSyncPayload payload, final IPayloadContext context) {
        AbstractAgent agent = AgentManager.getAgent(context.player().level(), payload.id());
        if (agent instanceof AbstractAgent) {
            AbstractAgentComponent component = agent.getComponents().get(payload.component());
            if (component!= null)
                context.enqueueWork(() -> component.getSyncedData().assignValues(payload.syncData()));
            else
                PyCraft.LOGGER.error("维度{}收到不存在智能体组件的同步数据包: {}", context.player().level().dimension().location(), payload.component());
        } else
            PyCraft.LOGGER.error("维度{}收到不存在智能体的组件同步数据包: {}", context.player().level().dimension().location(), payload.id());
    }
}
