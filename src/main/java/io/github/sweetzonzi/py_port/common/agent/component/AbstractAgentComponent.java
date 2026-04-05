package io.github.sweetzonzi.py_port.common.agent.component;

import cn.solarmoon.spark_core.physics.level.PhysicsLevel;
import io.github.sweetzonzi.py_port.common.agent.AbstractAgent;
import io.github.sweetzonzi.py_port.network.java.payload.AgentComponentSyncPayload;
import io.github.sweetzonzi.py_port.network.java.payload.AgentSyncPayload;
import lombok.Getter;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@Getter
abstract public class AbstractAgentComponent implements SyncedDataHolder {
    public final String name;
    public final AbstractAgent agent;
    /**
     * 同步数据存储器
     */
    protected final SynchedEntityData syncedData;

    public AbstractAgentComponent(String name, AbstractAgent agent) {
        this.name = name;
        this.agent = agent;
        // 服务端-客户端之间的同步数据
        SynchedEntityData.Builder syncheddata$builder = new SynchedEntityData.Builder(this);
        this.defineSyncedData(syncheddata$builder);
        this.syncedData = syncheddata$builder.build();
    }

    public void preTick() {
        if (!getLevel().isClientSide()) {
            syncToClient(); // 同步数据至客户端
        }
    }

    public void postTick() {
    }

    public void prePhysicsTick() {
    }

    public void postPhysicsTick() {
    }

    public Level getLevel() {
        return agent.getLevel();
    }

    public PhysicsLevel getPhysicsLevel() {
        return agent.getPhysicsLevel();
    }

    protected void defineSyncedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
    }

    @Override
    public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> list) {
    }

    /**
     * <p>立即同步发生变化的数据至客户端</p>
     */
    public void syncToClient() {
        if (!getLevel().isClientSide()) {
            SynchedEntityData synchedentitydata = this.getSyncedData();
            List<SynchedEntityData.DataValue<?>> list = synchedentitydata.packDirty();
            if (list != null) {
                PacketDistributor.sendToPlayersInDimension((ServerLevel) getLevel(), new AgentComponentSyncPayload(getAgent().getId(), getName(), list));
            }
        }
    }
}
