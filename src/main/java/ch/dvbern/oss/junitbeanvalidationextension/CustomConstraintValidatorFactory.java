package ch.dvbern.oss.junitbeanvalidationextension;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

import static java.util.Objects.requireNonNull;

class CustomConstraintValidatorFactory implements ConstraintValidatorFactory {

	private final Map<Class<?>, ConstraintValidator<?, ?>> validators;
	private final ConstraintValidatorFactory defaultFactory;

	public CustomConstraintValidatorFactory(
			Map<Class<?>, ConstraintValidator<?, ?>> validators,
			ConstraintValidatorFactory defaultFactory
	) {
		this.validators = new HashMap<>(validators);
		this.defaultFactory = requireNonNull(defaultFactory);
	}

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		@SuppressWarnings("unchecked")
		T validator = (T) validators.get(key);

		if (validator != null) {
			return validator;
		}

		T defaultValidator = defaultFactory.getInstance(key);
		return defaultValidator;
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		// is there something to be done for our own validators?

		defaultFactory.releaseInstance(instance);
	}

}
