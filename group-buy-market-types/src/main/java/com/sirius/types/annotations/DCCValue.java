package com.sirius.types.annotations;

import java.lang.annotation.*;

/**
 * @author Sirius
 * @description 注解，动态配置中心标记
 * @date 2026年09月21日 11:21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface DCCValue {

    String value() default "";

}
