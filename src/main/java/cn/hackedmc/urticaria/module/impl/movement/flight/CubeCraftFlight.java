package cn.hackedmc.urticaria.module.impl.movement.flight;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.impl.movement.Flight;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.MoveInputEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.NumberValue;

/**
 * @author Auth
 * @since 18/11/2021
 */

public class CubeCraftFlight extends Mode<Flight>  {

    private final NumberValue speed = new NumberValue("Speed", this, 1, 0.1, 9.5, 0.1);

    public CubeCraftFlight(String name, Flight parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {

        final float speed = this.speed.getValue().floatValue();

        event.setSpeed(speed);
    };

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        final float speed = this.speed.getValue().floatValue();

        InstanceAccess.mc.thePlayer.motionY = -1E-10D
                + (InstanceAccess.mc.gameSettings.keyBindJump.isKeyDown() ? speed : 0.0D)
                - (InstanceAccess.mc.gameSettings.keyBindSneak.isKeyDown() ? speed : 0.0D);


        boolean playerNearby = !Client.INSTANCE.getTargetManager().getTargets(12).isEmpty() ||
                InstanceAccess.mc.currentScreen != null;

        if (InstanceAccess.mc.thePlayer.getDistance(InstanceAccess.mc.thePlayer.lastReportedPosX, InstanceAccess.mc.thePlayer.lastReportedPosY, InstanceAccess.mc.thePlayer.lastReportedPosZ)
                <= (playerNearby ? 5 : 10) - speed - 0.15 && InstanceAccess.mc.thePlayer.swingProgressInt != 3) {
            event.setCancelled(true);
        }
    };


    @EventLink()
    public final Listener<MoveInputEvent> onMove = event -> {
        event.setSneak(false);
    };

    @Override
    public void onDisable() {
        MoveUtil.stop();
    }
}