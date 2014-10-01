package fr.logica.domain.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import fr.logica.business.Action.Input;
import fr.logica.business.Action.Persistence;
import fr.logica.business.Action.Process;
import fr.logica.business.Action.UserInterface;

@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Action {

	int code();
	String queryName() default "";
	String pageName() default "";

	int nextAction() default -1;

	Input input() default Input.ONE;
	Persistence persistence();
	UserInterface ui() default UserInterface.INPUT;
	Process process() default Process.STANDARD;

	int[] subActions() default {};

}
