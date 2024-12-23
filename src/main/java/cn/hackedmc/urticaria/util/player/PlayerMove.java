package cn.hackedmc.urticaria.util.player;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PlayerMove {
    public static double getXzTickSpeed(Vec3 lastTick, Vec3 currentTick) {
        return getXZOnlyPos(currentTick).distanceTo(getXZOnlyPos(lastTick));
    }

    public static double getXzSecSpeed(Vec3 lastTick, Vec3 currentTick) {
        return getXzTickSpeed(lastTick, currentTick) * 20;  // IDK what's the fucking tps
    }

    public static Vec3 getXZOnlyPos(Vec3 position) {
        return new Vec3(position.xCoord, 0, position.zCoord);
    }

    public static double getJumpDistance(AbstractClientPlayer player) {
        try {
            final int x = player.getActivePotionEffects().stream()
                    .filter(effect -> effect.getEffectName().toLowerCase().contains("jump"))
                    .findAny()
                    .orElseThrow(NullPointerException::new)
                    .getAmplifier() + 1;
//            return -9.331952072919326 * x * x - 3.672263213983712 * x + 0.6261016701268645;  // chat gpt
            return 0.04837 * x + 0.5356 * x + 1.252;  // numpy
        } catch (NullPointerException e) {
            return 1.252203340253729;
        }
    }

    public static boolean isNoMove(Vec3 motion) {
        return motion.xCoord == 0 && motion.zCoord == 0;
    }

    /**
     * Gets the players predicted jump motion the specified amount of ticks ahead
     *
     * @return predicted jump motion
     */
    public static double predictedMotion(final double motion, final int ticks) {
        if (ticks == 0) return motion;
        double predicted = motion;

        for (int i = 0; i < ticks; i++) {
            predicted = (predicted - 0.08) * 0.98F;
        }

        return predicted;
    }

    /**
     * Used to get the players speed
     */
    public static double speed(EntityPlayer player) {
        return speed(player.motionX, player.motionZ);
    }

    public static double speed(final double motionX, final double motionZ) {
        return Math.hypot(motionX, motionZ);
    }

    public static Vec3 getStrafeMotion(final double speed, final double yaw, final double motionY) {
        return new Vec3(-MathHelper.sin((float) yaw) * speed, motionY, MathHelper.cos((float) yaw) * speed);
    }

    /**
     * Gets the players' movement yaw
     */
    public static double direction(float moveForward, float moveStrafing, float rotationYaw) {
        if (moveForward < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (moveForward < 0) {
            forward = -0.5F;
        } else if (moveForward > 0) {
            forward = 0.5F;
        }

        if (moveStrafing > 0) {
            rotationYaw -= 70 * forward;
        }

        if (moveStrafing < 0) {
            rotationYaw += 70 * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    public static boolean isInvalidMotion(Vec3 motion) {
        return Math.abs(motion.xCoord) >= 3.9
                || Math.abs(motion.yCoord) >= 3.9
                || Math.abs(motion.zCoord) >= 3.9;
    }

    public static double getMaxXZDiff(Vec3 motion1, Vec3 motion2) {
        return Math.max(Math.abs(motion1.xCoord - motion2.xCoord), Math.abs(motion1.zCoord - motion2.zCoord));
    }

    public static List<Vec3> getPosHistoryDiff(final List<Vec3> posHistory) {
        List<Vec3> result = new ArrayList<>(posHistory.size() - 1);

        for (int i = 0; i < posHistory.size() - 1; i++) {
            result.add(posHistory.get(i + 1).subtract(posHistory.get(i)));
        }

        return result;
    }
}