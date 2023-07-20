package ch.dvbern.oss.junitbeanvalidationextension;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import jakarta.validation.ConstraintValidator;

import static java.util.stream.Collectors.toMap;

/**
 * Collection of predefined customizations for ease of use.
 */
public final class Customizations {
	private Customizations() {
		// utility class
	}

	/**
	 * Use your own ConstraintValidators.
	 *
	 * Useful mainly, if your validators need constructor args or otherwise cannot easily initialized
	 * in a testing environment.
	 */
	public static ContextCustomizer usingConstraintValidator(ConstraintValidator<?, ?>... constraints) {
		Map<Class<?>, ConstraintValidator<?, ?>> map = Arrays
				.stream(constraints)
				.collect(toMap(ConstraintValidator::getClass, Function.identity()));

		return (c, defaultFactory) -> c.constraintValidatorFactory(
				new CustomConstraintValidatorFactory(map, defaultFactory));
	}

}
