package cn.hackedmc.urticaria.util.font.impl.rise;

import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class FontUtil {

    private static final IResourceManager RESOURCE_MANAGER = InstanceAccess.mc.getResourceManager();

    /**
     * Method which gets a font by a resource name
     *
     * @param resource resource name
     * @param size     font size
     * @return font by resource
     */
    public static Font getResource(final String resource, final int size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, RESOURCE_MANAGER.getResource(new ResourceLocation(resource)).getInputStream()).deriveFont(Font.PLAIN, (float) size);
        } catch (final FontFormatException | IOException ignored) {
            return null;
        }
    }

    public static Font getResource(final String resource) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, RESOURCE_MANAGER.getResource(new ResourceLocation(resource)).getInputStream());
        } catch (final FontFormatException | IOException ignored) {
            return null;
        }
    }
}
