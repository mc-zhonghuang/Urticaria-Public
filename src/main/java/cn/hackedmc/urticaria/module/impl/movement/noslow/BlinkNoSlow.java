package cn.hackedmc.urticaria.module.impl.movement.noslow;

import cn.hackedmc.urticaria.component.impl.player.BlinkComponent;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.module.impl.movement.NoSlow;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.SlowDownEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BlinkNoSlow extends Mode<NoSlow> {
    public BlinkNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }
    private final ModeValue mode = new ModeValue("Unblock Mode", this)
            .add(new SubMode("SwitchItem"))
            .add(new SubMode("Release"))
            .setDefault("Release");
    private final NumberValue blinkTick = new NumberValue("Blink Tick", this, 3, 1, 5, 1);
    private final BooleanValue food = new BooleanValue("Food & Bow", this, false);
    private int usingTime;

    @Override
    public void onEnable() {
        usingTime = 0;
    }

    private void release() {
        if (mode.getValue().getName().equalsIgnoreCase("Release")) {
            mc.getNetHandler().addToSendQueueUnregistered(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
            mc.getNetHandler().addToSendQueueUnregistered(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
        } else {
            mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }
    }

    @EventLink
    private final Listener<SlowDownEvent> onSlowDown = event -> {
        if (mc.thePlayer.isUsingItem() && ((mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) || food.getValue())) event.setCancelled();
    };

    @EventLink
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer.isUsingItem()) {
            final Item item = InstanceAccess.mc.thePlayer.getCurrentEquippedItem().getItem();

            if (item instanceof ItemSword || food.getValue()) {
                BlinkComponent.blinking = true;
                usingTime++;

                if (usingTime == blinkTick.getValue().intValue()) {
                    release();
                    BlinkComponent.packets.forEach(packet -> {
                        mc.getNetHandler().addToSendQueueUnregistered(packet);
                    });
                    BlinkComponent.packets.clear();
                    mc.getNetHandler().addToSendQueueUnregistered(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                }
            }
        } else {
            BlinkComponent.blinking = false;
        }
    };
}
