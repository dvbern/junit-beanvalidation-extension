package ch.dvbern.oss.junitbeanvalidationextension.demo.fixtures;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomConstraintValidator implements ConstraintValidator<CustomConstraint, Object> {

	private final boolean ok;

	// maybe in production, this parameter gets injected by CDI/Spring
	@Inject
	// @Autowired
	public CustomConstraintValidator(boolean someConfiguration) {
		ok = someConfiguration;
	}

	@Override
	public void initialize(CustomConstraint constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		return ok;
	}
}
