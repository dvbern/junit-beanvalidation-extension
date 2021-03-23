package ch.dvbern.oss.junitbeanvalidationextension;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import javax.validation.ConstraintValidator;

import static java.util.stream.Collectors.toMap;

public final class Customizations {
	private Customizations() {
		// utility class
	}

	public static ContextCustomizer usingConstraintValidator(ConstraintValidator<?, ?>... constraints) {
		Map<Class<?>, ConstraintValidator<?, ?>> map = Arrays
				.stream(constraints)
				.collect(toMap(ConstraintValidator::getClass, Function.identity()));

		return (c, defaultFactory) -> c.constraintValidatorFactory(
				new CustomConstraintValidatorFactory(map, defaultFactory));
	}

}
