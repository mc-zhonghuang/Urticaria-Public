package cn.hackedmc.urticaria.module.impl.movement.flight;

import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.module.impl.movement.Flight;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.newevent.impl.other.TeleportEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author Alan
 * @since 18/11/2021
 */

public class MMCFlight extends Mode<Flight> {

    private boolean clipped;
    private int ticks;

    public MMCFlight(String name, Flight parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        clipped = false;
        ticks = 0;
    }

    @Override
    public void onDisable() {
        BlinkComponent.blinking = false;
        MoveUtil.stop();
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        ticks++;

        if (mc.thePlayer.onGround) {
            MoveUtil.stop();
        } else {
            return;
        }

        if (ticks == 1) {
            if (PlayerUtil.blockRelativeToPlayer(0, -2.5, 0).isFullBlock()) {
                mc.timer.timerSpeed = 0.1F;
                BlinkComponent.blinking = true;

                PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, MoveUtil.roundToGround(mc.thePlayer.posY - (2.5 - (Math.random() / 100))), mc.thePlayer.posZ, false));
                PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));

                clipped = true;

                mc.thePlayer.jump();
                MoveUtil.strafe(7 - Math.random() / 10);
            }
        }
    };

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {
        MoveUtil.strafe();
    };


    @EventLink()
    public final Listener<TeleportEvent> onTeleport = event -> {
        if (clipped) {
            event.setCancelled(true);
            clipped = false;
        }
    };
}