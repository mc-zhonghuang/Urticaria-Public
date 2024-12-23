package cn.hackedmc.urticaria.module.impl.combat;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render3DEvent;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.pathfinding.unlegit.MainPathFinder;
import cn.hackedmc.urticaria.util.pathfinding.unlegit.Vec3;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.newevent.impl.input.ClickEvent;
import cn.hackedmc.urticaria.value.impl.*;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import util.time.StopWatch;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * @author Alan
 * @since 11/17/2021
 */
@Rise
@ModuleInfo(name = "module.combat.teleportaura.name", description = "module.combat.teleportaura.description", category = Category.COMBAT)
public final class TeleportAura extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Single"))
            .add(new SubMode("Multiple"))
            .setDefault("Single");

    private final NumberValue range = new NumberValue("Range", this, 32, 3, 100, 0.1);
    private final BoundsNumberValue cps = new BoundsNumberValue("CPS", this, 10, 15, 1, 20, 1);

    private final BooleanValue render = new BooleanValue("Render", this, true);

    private final StopWatch clickStopWatch = new StopWatch();
    private List<Vec3> path;
    public Entity target;
    private long nextSwing;

    @Override
    public void onDisable() {
        target = null;
    }

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {

        /*
         * Getting targets and selecting the nearest one
         */
        final List<Entity> targets = Client.INSTANCE.getTargetManager().getTargets(range.getValue().doubleValue());

        if (targets.isEmpty()) {
            target = null;
            return;
        }

        target = targets.get(0);

        if (target == null || mc.thePlayer.isDead) {
            return;
        }

        /*
         * Doing the attack
         */
        this.doAttack(targets);
    };

    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {

        if (!render.getValue() || path == null || target == null) {
            return;
        }

        Vec3 lastVector = null;

        for (final Vec3 vector : path) {
            if (lastVector != null) {
                RenderUtil.drawLine(lastVector.getX(), lastVector.getY() + 0.01, lastVector.getZ(), vector.getX(), vector.getY() + 0.01, vector.getZ(), Color.WHITE, 1);
            }
            lastVector = vector;
        }
    };

    private void doAttack(final List<Entity> targets) {
        if (clickStopWatch.finished(this.nextSwing) && target != null && !mc.gameSettings.keyBindAttack.isKeyDown() && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
            final long clicks = Math.round(MathUtil.getRandom(this.cps.getValue().intValue(), this.cps.getSecondValue().intValue()));
            this.nextSwing = 1000 / clicks;

            /*
             * Attacking target
             */
            final double range = this.range.getValue().doubleValue();

            switch (this.mode.getValue().getName()) {
                case "Single": {
                    if (mc.thePlayer.getDistanceToEntity(target) <= range) {
                        this.attack(target);
                    }
                    break;
                }

                case "Multiple": {
                    targets.removeIf(target -> mc.thePlayer.getDistanceToEntity(target) > range);

                    if (!targets.isEmpty()) {
                        targets.forEach(this::attack);
                    }
                    break;
                }
            }

            this.clickStopWatch.reset();
        }
    }

    private void attack(Entity target) {
        mc.playerController.syncCurrentPlayItem();

        final AttackEvent event = new AttackEvent(target);
        Client.INSTANCE.getEventBus().handle(event);

        if (event.isCancelled()) {
            return;
        }

        target = event.getTarget();

        path = MainPathFinder.computePath(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), new Vec3(target.posX, target.posY, target.posZ), true);

        if (path == null) {
            return;
        }

        for (final Vec3 vector : path) {
            PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(vector.getX(), vector.getY(), vector.getZ(), true));
        }

        Client.INSTANCE.getEventBus().handle(new ClickEvent());
        mc.thePlayer.swingItem();

        PacketUtil.sendNoEvent(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

        Collections.reverse(path);

        for (final Vec3 vector : path) {
            PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(vector.getX(), vector.getY(), vector.getZ(), true));
        }

        if (this.getModule(Criticals.class).isEnabled() || mc.thePlayer.fallDistance > 0 && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.ridingEntity == null) {
            mc.thePlayer.onCriticalHit(target);
        }
    }
}