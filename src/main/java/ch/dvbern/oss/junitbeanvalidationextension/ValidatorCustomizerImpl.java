package ch.dvbern.oss.junitbeanvalidationextension;

import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

class ValidatorCustomizerImpl implements ValidatorCustomizer {
	private ContextCustomizer customizer = null;

	@Override
	public void customize(ContextCustomizer customizer) {
		this.customizer = customizer;
	}

	public ValidatorContext applyCustomizations(
			ValidatorContext ctx,
			ValidatorFactory defaultFactory
	) {
		if (customizer == null) {
			return ctx;
		}

		ValidatorContext result = customizer.apply(ctx, defaultFactory.getConstraintValidatorFactory());

		return result;
	}
}
