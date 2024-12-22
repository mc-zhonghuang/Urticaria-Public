package cn.hackedmc.urticaria.newevent.impl.other;


import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.newevent.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class ModuleToggleEvent implements Event {
    private Module module;
}