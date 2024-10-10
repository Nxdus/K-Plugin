package mc.CushyPro.KItemSkin.Used;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface RegData {

    String value() default "";

    Class<?> mapclass() default String.class;
}


