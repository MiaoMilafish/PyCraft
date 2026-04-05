package io.github.sweetzonzi.py_port.client.render;

import cn.solarmoon.spark_core.animation.renderer.ModelRenderHelperKt;
import cn.solarmoon.spark_core.physics.level.PhysicsLevel;
import cn.solarmoon.spark_core.visual_effect.VisualEffectRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sweetzonzi.py_port.PyCraft;
import io.github.sweetzonzi.py_port.common.agent.AbstractAgent;
import io.github.sweetzonzi.py_port.common.agent.AgentManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Brightness;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = PyCraft.MOD_ID)
public class AgentRenderer extends VisualEffectRenderer {
    public static final AgentRenderer INSTANCE = new AgentRenderer();

    private static final Set<AbstractAgent> agents = new HashSet<>();

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
    }

    @Override
    public void tick() {
        agents.clear();
        if (Minecraft.getInstance().level != null)
            agents.addAll(AgentManager.getLevelAgents(Minecraft.getInstance().level));
    }

    @Override
    public void physTick(@NotNull PhysicsLevel physicsLevel) {

    }

    @Override
    public void render(@NotNull Minecraft minecraft, @NotNull Vec3 camPos, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, float partialTicks) {
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        for (AbstractAgent agent : agents) {
            poseStack.pushPose();
            poseStack.mulPose(agent.getWorldPositionMatrix(partialTicks));
            ModelRenderHelperKt.render(
                    agent,
                    poseStack,
                    multiBufferSource.getBuffer(RenderType.entityTranslucent(agent.getModelController().getTextureLocation())),
                    Brightness.FULL_BRIGHT.pack(),
                    OverlayTexture.NO_OVERLAY,
                    new Color(255, 255, 255, 255).getRGB(),
                    partialTicks
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
