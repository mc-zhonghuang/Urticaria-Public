package cn.hackedmc.urticaria.module.impl.movement.wallclimb;

import cn.hackedmc.urticaria.module.impl.movement.WallClimb;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.value.Mode;

/**
 * @author Nicklas
 * @since 05.06.2022
 */

public class KauriWallClimb extends Mode<WallClimb> {

    public KauriWallClimb(String name, WallClimb parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (InstanceAccess.mc.thePlayer.isCollidedHorizontally) {
            if (InstanceAccess.mc.thePlayer.ticksExisted % 3 == 0) {
                event.setOnGround(true);
                InstanceAccess.mc.thePlayer.jump();
            }
        }
    };
}