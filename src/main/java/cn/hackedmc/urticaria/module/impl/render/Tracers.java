package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.Render3DEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

import java.awt.*;

@Rise
@ModuleInfo(name = "module.render.tracers.name", description = "module.render.tracers.description", category = Category.RENDER)
public final class Tracers extends Module {

    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (mc.gameSettings.hideGUI) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        mc.entityRenderer.orientCamera(mc.timer.renderPartialTicks);

        for (final Entity player : Client.INSTANCE.getTargetManager()) {
            if (player == mc.thePlayer || player.isDead || Client.INSTANCE.getBotManager().contains(player)) {
                continue;
            }

            final double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
            final double y = (player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks()) + 1.62F;
            final double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

            final Color color = ColorUtil.withAlpha(
                    ColorUtil.mixColors(getTheme().getSecondColor(), getTheme().getFirstColor(), Math.min(1, mc.thePlayer.getDistanceToEntity(player) / 50)),
                128);

            RenderUtil.drawLine(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY + mc.thePlayer.getEyeHeight(), mc.getRenderManager().renderPosZ, x, y, z, color, 1.5F);
        }

        GlStateManager.resetColor();
        GlStateManager.popMatrix();
    };
}