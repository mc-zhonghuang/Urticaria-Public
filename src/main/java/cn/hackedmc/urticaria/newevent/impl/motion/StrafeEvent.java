package cn.hackedmc.urticaria.newevent.impl.motion;

import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.newevent.CancellableEvent;
import cn.hackedmc.urticaria.newevent.Event;
import cn.hackedmc.urticaria.script.api.wrapper.impl.event.ScriptEvent;
import cn.hackedmc.urticaria.script.api.wrapper.impl.event.impl.ScriptStrafeEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class StrafeEvent extends CancellableEvent implements InstanceAccess {

    private float forward;
    private float strafe;
    private float friction;
    private float yaw;

    public void setSpeed(final double speed, final double motionMultiplier) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        mc.thePlayer.motionX *= motionMultiplier;
        mc.thePlayer.motionZ *= motionMultiplier;
    }

    public void setSpeed(final double speed) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        MoveUtil.stop();
    }

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptStrafeEvent(this);
    }
}
