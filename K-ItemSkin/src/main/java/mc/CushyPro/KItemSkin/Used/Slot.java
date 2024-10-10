package mc.CushyPro.KItemSkin.Used;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ElementType.TYPE})
public @interface Slot {

    int[] value();

    String custom() default "";

    int x() default -1;

    int y() default -1;

    int tx() default -1;

    int ty() default -1;

    int priority() default 0;

}
