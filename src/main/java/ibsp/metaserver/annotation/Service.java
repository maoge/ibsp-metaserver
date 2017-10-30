package ibsp.metaserver.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface Service {
	String id() default "";
	String visible() default "public";
	String name();
	boolean auth() default true;
	boolean bwswitch() default true;
	Property[] properties() default {};
	Argu[] arguments() default {};
}
