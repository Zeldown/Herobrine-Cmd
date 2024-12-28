package be.zeldown.herobrinecmd.lib.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import be.zeldown.herobrinecmd.lib.SenderType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubCommand {

	String command();
	String description()  default "";
	String permission()   default "";
	SenderType[] sender() default SenderType.NONE;
	boolean help()        default true;
	int priority()        default 0;

}