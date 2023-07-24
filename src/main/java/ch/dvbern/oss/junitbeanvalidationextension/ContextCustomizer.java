package ch.dvbern.oss.junitbeanvalidationextension;

import java.util.function.BiFunction;

import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ValidatorContext;

@FunctionalInterface
public interface ContextCustomizer extends BiFunction<ValidatorContext, ConstraintValidatorFactory, ValidatorContext> {

	/**
	 * Just returns the unmodified ValidatorContext
	 */
	static ContextCustomizer identity() {
		return (t, ignored) -> t;
	}
}
