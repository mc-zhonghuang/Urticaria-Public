package cn.hackedmc.urticaria.module.impl.other;

import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.DevelopmentFeature;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.value.impl.DragValue;

import java.awt.*;

/**
 * @author Hazsi
 * @since 10/27/2022
 */
@DevelopmentFeature // This isn't done yet
@ModuleInfo(name = "module.other.spotify.name", description = "module.other.spotify.description", category = Category.OTHER)
public class Spotify extends Module {
    public final DragValue positionValue = new DragValue("Position", this, new Vector2d(200, 200));

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        positionValue.setScale(new Vector2d(180, 65));

        final double x = positionValue.position.getX() + 65;

        final Color backgroundColor = getTheme().getDropShadow(), backgroundColor2 = getTheme().getDropShadow();
        final String song = "No Time To Explain", artist = "Good Kid", progress = "1:43", length = "2:56";

        // Blur and bloom
        NORMAL_BLUR_RUNNABLES.add(() -> RenderUtil.roundedRectangle(positionValue.position.getX(), positionValue.position.getY(),
                positionValue.scale.getX(), positionValue.scale.getY(), 10, Color.BLACK));
        NORMAL_POST_BLOOM_RUNNABLES.add(() -> RenderUtil.drawRoundedGradientRect(positionValue.position.getX(), positionValue.position.getY(),
                positionValue.scale.getX(), positionValue.scale.getY(), 11, backgroundColor, backgroundColor2, false));

        NORMAL_RENDER_RUNNABLES.add(() -> {
            // Background
            RenderUtil.drawRoundedGradientRect(positionValue.position.getX(), positionValue.position.getY(), positionValue.scale.getX(),
                    positionValue.scale.getY(), 10, getTheme().getBackgroundShade(),
                    getTheme().getBackgroundShade(), false);

            // Song and artist name
            FontManager.getProductSansBold(20).drawString(song, x, positionValue.position.getY() + 15, Color.WHITE.getRGB());
            FontManager.getProductSansBold(16).drawString(artist, x, positionValue.position.getY() + 28, new Color(255, 255, 255, 128).getRGB());

            // Progress background
            RenderUtil.roundedRectangle(x, positionValue.position.getY() + 45, 100, 6, 3, getTheme().getBackgroundShade());

            // Progress bar
            RenderUtil.drawRoundedGradientRect(x, positionValue.position.getY() + 45, 75, 6, 3,
                    getTheme().getFirstColor(), getTheme().getSecondColor(), true);

            // Time and duration
            FontManager.getProductSansBold(15).drawRightString(progress + " / ",
                    x + 100 - FontManager.getProductSansBold(15).width(length), positionValue.position.getY() + 35,
                    new Color(255, 255, 255, 128).getRGB());
            FontManager.getProductSansBold(15).drawRightString(length, x + 100, positionValue.position.getY() + 35,
                    new Color(255, 255, 255, 48).getRGB());
        });
    };
}
