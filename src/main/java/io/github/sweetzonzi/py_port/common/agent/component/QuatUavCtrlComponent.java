package io.github.sweetzonzi.py_port.common.agent.component;

import io.github.sweetzonzi.py_port.common.agent.AbstractAgent;

import java.util.List;

public class QuatUavCtrlComponent extends AbstractAgentComponent {
    private final List<ThrusterComponent> thrusters;

    public QuatUavCtrlComponent(String name, AbstractAgent agent, List<ThrusterComponent> thrusters) {
        super(name, agent);
        this.thrusters = thrusters;
    }

    @Override
    public void prePhysicsTick() {
        super.prePhysicsTick();
        //TODO: 根据目标位置/姿态/速度/角速度，调节各推进器推力
    }
}
