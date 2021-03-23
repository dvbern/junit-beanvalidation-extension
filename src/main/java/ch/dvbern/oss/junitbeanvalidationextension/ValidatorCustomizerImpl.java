package ch.dvbern.oss.junitbeanvalidationextension;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.ValidatorContext;

class ValidatorCustomizerImpl implements ValidatorCustomizer {
	private ContextCustomizer customizer = ContextCustomizer.identity();

	@Override
	public void customize(ContextCustomizer customizer) {
		this.customizer = customizer;
	}

	public ValidatorContext applyCustomizations(
			ValidatorContext ctx,
			ConstraintValidatorFactory defaultFactory
	) {
		ValidatorContext result = customizer.apply(ctx, defaultFactory);

		return result;
	}
}
