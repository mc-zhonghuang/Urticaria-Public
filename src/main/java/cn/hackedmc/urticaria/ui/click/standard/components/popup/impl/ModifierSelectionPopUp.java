package cn.hackedmc.urticaria.ui.click.standard.components.popup.impl;

import cn.hackedmc.urticaria.ui.click.standard.components.popup.PopUp;
import cn.hackedmc.urticaria.util.vector.Vector2f;

/**
 * Author: Alan
 * Date: 29/03/2022
 */

public class ModifierSelectionPopUp extends PopUp {
    @Override
    public void draw() {
        this.scale = new Vector2f(200, 120);
        super.draw();
    }
}
