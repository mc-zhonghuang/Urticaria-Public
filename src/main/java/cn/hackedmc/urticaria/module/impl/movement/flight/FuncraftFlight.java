package cn.hackedmc.urticaria.module.impl.movement.flight;

import cn.hackedmc.urticaria.module.impl.movement.Flight;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.MoveEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author Wykt
 * @since 2/04/2023
 */

public final class FuncraftFlight extends Mode<Flight> {
    public FuncraftFlight(String name, Flight parent) {
        super(name, parent);
    }

    private final NumberValue speed = new NumberValue("Speed", this, 1.2, 0.8, 2, 0.05);
    private final BooleanValue vanillaKickBypass = new BooleanValue("Vanilla Kick Bypass", this, true);

    private double moveSpeed;
    private int stage, ticks;

    @Override
    public void onEnable() {
        moveSpeed = 0;
        stage = mc.thePlayer.onGround ? 0 : -1;
        ticks = 0;
    }

    @EventLink
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        event.setOnGround(true);
    };

    @EventLink(Priorities.VERY_HIGH)
    private final Listener<MoveEvent> moveEventListener = event -> {
        if(!MoveUtil.isMoving() || mc.thePlayer.isCollidedHorizontally) {
            stage = -1;
        }

        // vanilla kick bypass
        if(vanillaKickBypass.getValue() && ticks > 125) {
            stage = -1;
            ticks = 0;
            PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 5, mc.thePlayer.posY + 1, mc.thePlayer.posZ + 5, true));
            return;
        }

        switch(stage) {
            case -1:
                mc.thePlayer.motionY = 0;
                event.setPosY(-0.00001);
                return;
            case 0:
                moveSpeed = 0.3;
                break;
            case 1:
                if(mc.thePlayer.onGround) {
                    event.setPosY(mc.thePlayer.motionY = 0.3999);
                    moveSpeed *= 2.14;
                }
                break;
            case 2:
                moveSpeed = speed.getValue().doubleValue();
                break;
            default:
                moveSpeed -= moveSpeed / 159;
                mc.thePlayer.motionY = 0;
                event.setPosY(-0.00001);
                break;
        }

        mc.thePlayer.jumpMovementFactor = 0F;
        MoveUtil.setMoveEvent(event, Math.max(moveSpeed, MoveUtil.getAllowedHorizontalDistance()));
        stage++;
    };
}