package cn.hackedmc.urticaria.module.impl.movement.teleport;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.module.impl.movement.Teleport;
import cn.hackedmc.urticaria.module.api.DevelopmentFeature;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.TeleportEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render3DEvent;
import cn.hackedmc.urticaria.util.animation.Animation;
import cn.hackedmc.urticaria.util.animation.Easing;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@Rise
@DevelopmentFeature
public final class WatchdogTeleport extends Mode<Teleport> {
    private boolean offset;
    private Vec3 position = new Vec3(0, 0, 0);
    private int blocks;
    private double savedOffset;
    private Animation animation = new Animation(Easing.LINEAR, 200);

    public WatchdogTeleport(String name, Teleport parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<TeleportEvent> teleport = event -> {
        toggle();
    };

    @EventLink
    public final Listener<Render2DEvent> render2D = event -> {

        FontManager.getProductSansLight(18).
                drawCenteredString(blocks + " blocks required to get to this position, use sneak to activate",
                        mc.scaledResolution.getScaledWidth() / 2f, mc.scaledResolution.getScaledHeight() / 2f + 30,
                        getTheme().getFirstColor().getRGB());

    };

    @EventLink
    public final Listener<Render3DEvent> render3D = event -> {
        final float partialTicks = mc.timer.renderPartialTicks;

        EntityPlayer player = mc.thePlayer;

        final Color color = ColorUtil.withAlpha(getTheme().getFirstColor(), 100);

        if (player.getEntityBoundingBox() == null || color.getAlpha() <= 0) return;

        final double yaw = Math.toRadians(mc.thePlayer.prevRotationYaw + (mc.thePlayer.rotationYaw - mc.thePlayer.prevRotationYaw) * partialTicks);
        final double speed = 7.8;

        double x = (player.prevPosX + (player.posX - player.prevPosX) * partialTicks) - Math.sin(yaw) * speed;
        double y = player.prevPosY + (player.posY - player.prevPosY) * partialTicks - 2;
        double z = (player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks) + Math.cos(yaw) * speed;

        double o = 0;
        for (int offset = 0; offset <= 4; offset++) {
            if (PlayerUtil.block(x, y - 1 + offset, z).isFullBlock() && !PlayerUtil.block(x, y + offset, z).isFullBlock()) {
                o = offset;
            }
        }

        animation.setDuration(50);
        animation.run(o);
        savedOffset = animation.getValue();
        y += savedOffset;

        position = new Vec3(x, y, z);

        RenderUtil.color(color);

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glDepthMask(false);

        double expand = 0.14;

        RenderUtil.drawBoundingBox(player.getEntityBoundingBox().offset(-player.posX, -player.posY, -player.posZ).
                offset(x, y, z).expand(expand, expand, expand));

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GL11.glDepthMask(true);
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GlStateManager.resetColor();

        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
    };

    @EventLink
    public final Listener<PreUpdateEvent> preUpdate = event -> {
        blocks = (int) (position.yCoord - (mc.thePlayer.posY - 2));

        if (!mc.gameSettings.keyBindSneak.isKeyDown()) return;

        if (blocks == 0) {
            offset = !offset;
            PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(position.xCoord + (offset ? 0.005 : -0.005), position.yCoord, position.zCoord + (offset ? 0.005 : -0.005), false));
        }
    };
}
