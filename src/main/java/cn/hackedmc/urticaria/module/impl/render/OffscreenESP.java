package cn.hackedmc.urticaria.module.impl.render;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.component.impl.render.ProjectionComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.LimitedRender2DEvent;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.value.impl.ColorValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector4d;
import java.awt.*;

@Rise
@ModuleInfo(name = "module.render.offscreenesp.name", description = "module.render.offscreenesp.description", category = Category.RENDER)
public final class OffscreenESP extends Module  {

    private ColorValue color = new ColorValue("Color", this, Color.RED);

    @EventLink()
    public final Listener<LimitedRender2DEvent> onLimitedRender2D = event -> {

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (mc.getRenderManager() == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) ||
                    RenderUtil.isInViewFrustrum(player) || player.isDead || player.isInvisible()) {
                continue;
            }

            Vector4d position = ProjectionComponent.get(player);

            if (position == null) {
                return;
            }

            Vector2d middlePosition = new Vector2d(position.x + (position.z - position.x) / 2, position.y + (position.w - position.y) / 2);

            double angle = Math.toDegrees(Math.atan2(middlePosition.x, middlePosition.y));

            ChatUtil.display(player.getCommandSenderName() + " " + angle);
            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) angle, 0, 1, 0);
            GlStateManager.translate(event.getScaledResolution().getScaledWidth() / 2f, event.getScaledResolution().getScaledHeight() / 2f, 0);
            RenderUtil.rectangle(0, 0, 1, 10, Color.BLACK);
            GlStateManager.popMatrix();
        }
    };
}