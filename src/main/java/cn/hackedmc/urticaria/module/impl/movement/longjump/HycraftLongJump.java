package cn.hackedmc.urticaria.module.impl.movement.longjump;

import cn.hackedmc.urticaria.module.impl.movement.LongJump;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;

public class HycraftLongJump extends Mode<LongJump> {
    public HycraftLongJump(String name, LongJump parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        mc.thePlayer.jump();
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        mc.thePlayer.motionY += 0.05999;
        MoveUtil.strafe(MoveUtil.speed() * 1.08f);
    };
}
