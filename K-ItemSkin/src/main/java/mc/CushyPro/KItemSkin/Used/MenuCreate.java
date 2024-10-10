package mc.CushyPro.KItemSkin.Used;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({TYPE, CONSTRUCTOR})
public @interface MenuCreate {

    int id() default 0;

    int size();

    CancelSlot canceltype() default CancelSlot.ALL;

}
