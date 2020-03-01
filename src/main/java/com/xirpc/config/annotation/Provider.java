package com.xirpc.config.annotation;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface Provider {

    Class<?> interfaceClass() default void.class;
    
    String interfaceName() default "";

    String version() default "";

    String group() default "";

    String path() default "";

    boolean export() default true;

    String token() default "";

    boolean deprecated() default false;

    boolean dynamic() default true;

    String accesslog() default "";

    int executes() default 0;

    boolean register() default true;

    int weight() default 0;

    String document() default "";

    
    int delay() default 0;

    
    String local() default "";

    
    String stub() default "";

    
    String cluster() default "";

    
    String proxy() default "";

    
    int connections() default 0;

    
    int callbacks() default 0;

    
    String onconnect() default "";

    
    String ondisconnect() default "";

    
    String owner() default "";

    
    String layer() default "";

    
    int retries() default 1;

    
    String loadbalance() default "";

    
    boolean async() default false;

    
    int actives() default 0;

    
    boolean sent() default false;

    
    String mock() default "";

    
    String validation() default "";

    
    int timeout() default 0;

    
    String cache() default "";

    
    String[] filter() default {};

    
    String[] listener() default {};

    
    String[] parameters() default {};

    
    String application() default "";

    
    String module() default "";

    
    String provider() default "";

    
    String[] protocol() default {};

    
    String monitor() default "";

    
    String[] registry() default {};

    
    String tag() default "";

    
    Method[] methods() default {};
}
