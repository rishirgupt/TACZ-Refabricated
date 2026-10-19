package com.tacz.guns.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(Holder.class)
public @interface ValueModifiableAtRuntime {
   Class<?> value();
}
