package ch.dvbern.oss.junitbeanvalidationextension.demo;

import java.util.Set;

import ch.dvbern.oss.junitbeanvalidationextension.Customizations;
import ch.dvbern.oss.junitbeanvalidationextension.ValidatorCustomizer;
import ch.dvbern.oss.junitbeanvalidationextension.ValidatorExtension;
import ch.dvbern.oss.junitbeanvalidationextension.demo.fixtures.CustomConstraintValidator;
import ch.dvbern.oss.junitbeanvalidationextension.demo.fixtures.SomeFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

// Enable the extension
@ExtendWith(ValidatorExtension.class)
public class CustomValidatorTest {

	// global setup for all followup tests
	// ValidatorCustomizer gets injected by the extension
	@BeforeAll
	static void beforeAll(ValidatorCustomizer customizer) {
		// 'Customizations' contains some predefined routines for ease of use.
		customizer.customize(Customizations.usingConstraintValidator(
				new CustomConstraintValidator(true)));
	}

	@Nested
	class ValidInputTest {
		// the Validator param gets injected by the extension
		@Test
		void validator_accepts_valid_input(Validator validator) {
			Set<ConstraintViolation<Object>> actual = validator.validate(new SomeFixture("Hello World"));

			assertThat(actual)
					.isEmpty();
		}
	}

	@Nested
	class ChangedConfigurationTest {

		// that one test that needs special setup
		@BeforeEach
		void beforeEach(ValidatorCustomizer customizer) {
			// please note: beforeEach customization completely *replaces* customization in beforeAll!
			customizer.customize(Customizations.usingConstraintValidator(
					new CustomConstraintValidator(false)));
		}

		@Test
		void rejects_same_input_with_changed_configuration(Validator validator) {
			Set<ConstraintViolation<Object>> actual = validator.validate(new SomeFixture("Hello World"));

			assertThat(actual)
					.isNotEmpty();
		}
	}
}
