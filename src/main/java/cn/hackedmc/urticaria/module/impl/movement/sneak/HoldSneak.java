package cn.hackedmc.urticaria.module.impl.movement.sneak;

import cn.hackedmc.urticaria.module.impl.movement.Sneak;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.value.Mode;
import org.lwjgl.input.Keyboard;

/**
 * @author Auth
 * @since 25/06/2022
 */

public class HoldSneak extends Mode<Sneak> {

    public HoldSneak(String name, Sneak parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.gameSettings.keyBindSneak.setPressed(true);
    };

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindSneak.setPressed(Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
    }
}