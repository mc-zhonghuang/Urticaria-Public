package cn.hackedmc.urticaria.module.impl.combat.velocity;

import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.module.impl.combat.Velocity;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.value.Mode;
import net.minecraft.util.MovingObjectPosition;

public final class IntaveVelocity extends Mode<Velocity> {

    private boolean attacked;

    public IntaveVelocity(String name, Velocity parent) {
        super(name, parent);
    }


    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (getParent().onSwing.getValue() || getParent().onSprint.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY) && mc.thePlayer.hurtTime > 0 && !attacked) {
            mc.thePlayer.motionX *= 0.6D;
            mc.thePlayer.motionZ *= 0.6D;
            mc.thePlayer.setSprinting(false);
        }

        attacked = false;
    };

    @EventLink()
    public final Listener<AttackEvent> onAttack = event -> {
        attacked = true;
    };
}
