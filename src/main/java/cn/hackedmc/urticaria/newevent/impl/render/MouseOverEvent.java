package cn.hackedmc.urticaria.newevent.impl.render;

import cn.hackedmc.urticaria.newevent.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Alan
 * @since 13.03.2022
 */
@Getter
@Setter
@AllArgsConstructor
public class MouseOverEvent implements Event {

    private double range;
    private float expand;

}
