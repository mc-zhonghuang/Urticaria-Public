package cn.hackedmc.urticaria.module.impl.player.scaffold.tower;

import cn.hackedmc.urticaria.module.impl.player.Scaffold;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public class    NCPTower extends Mode<Scaffold> {

    public NCPTower(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (mc.gameSettings.keyBindJump.isKeyDown() && PlayerUtil.blockNear(2)) {
            PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(null));

//            mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;

            if (mc.thePlayer.posY % 1 <= 0.00153598) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, Math.floor(mc.thePlayer.posY), mc.thePlayer.posZ);
                mc.thePlayer.motionY = 0.42F;
            } else if (mc.thePlayer.posY % 1 < 0.1 && mc.thePlayer.offGroundTicks != 0) {
                mc.thePlayer.motionY = 0;
                mc.thePlayer.setPosition(mc.thePlayer.posX, Math.floor(mc.thePlayer.posY), mc.thePlayer.posZ);
            }
        }
    };
}
