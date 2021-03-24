package ch.dvbern.oss.junitbeanvalidationextension.demo.fixtures;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = CustomConstraintValidator.class)
public @interface CustomConstraint {
	String message() default "{CustomConstraint.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
