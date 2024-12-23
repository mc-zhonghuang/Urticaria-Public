package cn.hackedmc.urticaria.module.impl.movement;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.PacketlessDamageComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.movement.longjump.*;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;

/**
 * @author Auth
 * @since 3/02/2022
 */
@Rise
@ModuleInfo(name = "module.movement.longjump.name", description = "module.movement.longjump.description", category = Category.MOVEMENT)
public class LongJump extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new VanillaLongJump("Vanilla", this))
            .add(new NCPLongJump("NCP", this))
            .add(new WatchdogLongJump("Watchdog", this))
            .add(new VulcanLongJump("Vulcan", this))
            .add(new ExtremeCraftLongJump("Extreme Craft", this))
            .add(new MatrixLongJump("Matrix", this))
            .add(new FireBallLongJump("Fire Ball", this))
            .add(new HycraftLongJump("Hycraft", this))
            .setDefault("Vanilla");

    private final BooleanValue autoDisable = new BooleanValue("Auto Disable", this, true);
    private final BooleanValue fakeDamage = new BooleanValue("Fake Damage", this, false);

    private boolean inAir;

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {

        if (!mode.getValue().getName().equalsIgnoreCase("Watchdog") && autoDisable.getValue() && inAir && mc.thePlayer.onGround) {
            this.toggle();
        }

        inAir = !mc.thePlayer.onGround && !PacketlessDamageComponent.isActive();
    };

    @Override
    protected void onEnable() {
        if (fakeDamage.getValue() && mc.thePlayer.ticksExisted > 1) {
            PlayerUtil.fakeDamage();
        }
    }

    @Override
    protected void onDisable() {
        mc.timer.timerSpeed = 1.0F;
        inAir = false;
    }
}
