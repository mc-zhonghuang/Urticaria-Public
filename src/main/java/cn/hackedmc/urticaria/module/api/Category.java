package cn.hackedmc.urticaria.module.api;

import cn.hackedmc.urticaria.ui.click.standard.screen.Screen;
import cn.hackedmc.urticaria.ui.click.standard.screen.impl.CategoryScreen;
import cn.hackedmc.urticaria.ui.click.standard.screen.impl.LanguageScreen;
import cn.hackedmc.urticaria.ui.click.standard.screen.impl.SearchScreen;
import cn.hackedmc.urticaria.ui.click.standard.screen.impl.ThemeScreen;
import cn.hackedmc.urticaria.util.font.Font;
import cn.hackedmc.urticaria.util.font.FontManager;
import cn.hackedmc.urticaria.Type;
import lombok.Getter;

/**
 * @author Patrick
 * @since 10/19/2021
 */
@Getter
public enum Category {
    SEARCH("category.search", FontManager.getIconsThree(17), "U", 0x1, new SearchScreen(), Type.BOTH),
    COMBAT("category.combat", FontManager.getIconsOne(17), "a", 0x2, new CategoryScreen(), Type.BASIC),
    MOVEMENT("category.movement", FontManager.getIconsOne(17), "b", 0x3, new CategoryScreen(), Type.BASIC),
    PLAYER("category.player", FontManager.getIconsOne(17), "c", 0x4, new CategoryScreen(), Type.BASIC),
    RENDER("category.render", FontManager.getIconsOne(17), "g", 0x5, new CategoryScreen(), Type.BASIC),
    EXPLOIT("category.exploit", FontManager.getIconsOne(17), "a", 0x6, new CategoryScreen(), Type.BASIC),
    GHOST("category.ghost", FontManager.getIconsOne(17), "f", 0x7, new CategoryScreen(), Type.BASIC),
    OTHER("category.other", FontManager.getIconsOne(17), "e", 0x8, new CategoryScreen(), Type.BASIC),
    SCRIPT("category.script", FontManager.getIconsThree(17), "m", 0x7, new CategoryScreen(), Type.BASIC),

    THEME("category.themes", FontManager.getIconsThree(17), "U", 0xA, new ThemeScreen(), Type.BOTH),

    LANGUAGE("category.language",FontManager.getIconsThree(17), "U",0xA,new LanguageScreen(), Type.BOTH);

    // name of category (in case we don't use enum names)
    private final String name;

    // icon character
    private final String icon;

    // optional color for every specific category (module list or click gui)
    private final int color;

    private final Font fontRenderer;

    public final Screen clickGUIScreen;
    public final Type clientType;

    Category(final String name, final Font fontRenderer, final String icon, final int color, final Screen clickGUIScreen, Type clientType) {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.clickGUIScreen = clickGUIScreen;
        this.fontRenderer = fontRenderer;
        this.clientType = clientType;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }
}