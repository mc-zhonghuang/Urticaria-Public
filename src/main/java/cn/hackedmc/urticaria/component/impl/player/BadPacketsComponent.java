package cn.hackedmc.urticaria.component.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.Component;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;

import static net.minecraft.network.play.client.C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT;

@Rise
public final class BadPacketsComponent extends Component {

    private static boolean slot, attack, swing, block, inventory;

    public static boolean bad() {
        return bad(true, true, true, true, true);
    }

    public static boolean bad(final boolean slot, final boolean attack, final boolean swing, final boolean block, final boolean inventory) {
        return (BadPacketsComponent.slot && slot) ||
                (BadPacketsComponent.attack && attack) ||
                (BadPacketsComponent.swing && swing) ||
                (BadPacketsComponent.block && block) ||
                (BadPacketsComponent.inventory && inventory);
    }

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<PacketSendEvent> onPacketSend = event -> {

        final Packet<?> packet = event.getPacket();

        if (packet instanceof C09PacketHeldItemChange) {
            slot = true;
        } else if (packet instanceof C0APacketAnimation) {
            swing = true;
        } else if (packet instanceof C02PacketUseEntity) {
            attack = true;
        } else if (packet instanceof C08PacketPlayerBlockPlacement || packet instanceof C07PacketPlayerDigging) {
            block = true;
        } else if (packet instanceof C0EPacketClickWindow ||
                (packet instanceof C16PacketClientStatus && ((C16PacketClientStatus) packet).getStatus() == OPEN_INVENTORY_ACHIEVEMENT) ||
                packet instanceof C0DPacketCloseWindow) {
            inventory = true;
        }
    };

    @EventLink(value = Priorities.VERY_LOW)
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (!event.isCancelled())
            reset();
    };

    public static void reset() {
        slot = false;
        swing = false;
        attack = false;
        block = false;
        inventory = false;
    }
}
