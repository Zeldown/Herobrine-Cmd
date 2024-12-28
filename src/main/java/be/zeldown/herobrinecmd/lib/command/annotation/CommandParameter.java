package be.zeldown.herobrinecmd.lib.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandParameter {

	String description() default "";
	String error() default "";
	String[] autocomplete() default {};

}