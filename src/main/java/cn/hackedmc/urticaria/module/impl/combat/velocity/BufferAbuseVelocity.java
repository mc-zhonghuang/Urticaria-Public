package cn.hackedmc.urticaria.module.impl.combat.velocity;

import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.module.impl.combat.Velocity;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public final class BufferAbuseVelocity extends Mode<Velocity> {

    private final NumberValue horizontal = new NumberValue("Horizontal", this, 100, 0, 100, 1);
    private final NumberValue vertical = new NumberValue("Vertical", this, 100, 0, 100, 1);
    private final NumberValue buffer = new NumberValue("Buffer", this, 1, 1, 3, 1);

    private int amount;

    public BufferAbuseVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        if (getParent().onSwing.getValue() || getParent().onSprint.getValue() && !mc.thePlayer.isSwingInProgress) return;

        final Packet<?> p = event.getPacket();

        final double horizontal = this.horizontal.getValue().doubleValue();
        final double vertical = this.vertical.getValue().doubleValue();

        if (p instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapper = (S12PacketEntityVelocity) p;

            if (wrapper.getEntityID() == mc.thePlayer.getEntityId()) {
                if (amount < buffer.getValue().intValue()) {
                    event.setCancelled(true);
                    amount++;
                    return;
                }

                wrapper.motionX *= horizontal / 100;
                wrapper.motionY *= vertical / 100;
                wrapper.motionZ *= horizontal / 100;

                event.setPacket(wrapper);
                amount = 0;
            }
        }

        if (p instanceof S27PacketExplosion) {
            final S27PacketExplosion wrapper = (S27PacketExplosion) p;

            if (amount < buffer.getValue().intValue()) {
                event.setCancelled(true);
                amount++;
                return;
            }

            wrapper.posX *= horizontal / 100;
            wrapper.posY *= vertical / 100;
            wrapper.posZ *= horizontal / 100;

            event.setPacket(wrapper);
            amount = 0;
        }
    };
}
