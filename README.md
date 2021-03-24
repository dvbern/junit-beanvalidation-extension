# junit-beanvalidation-extension

Allow for easy injection and customization of a Java BeanValidation Validator into your tests.

This enables you to use ConstraintValidators that need special setup (like e.g. Mocks).

## Prerequisites/Dependencies

This project requires Java >= 8.

Installing

Maven dependency:

```xml

<dependency>
	<groupId>ch.dvbern.oss</groupId>
	<artifactId>junit-beanvalidation-extension</artifactId>
	<version>see-github-releases</version>
	<scope>test</scope>
</dependency>
```

Current version: see [GitHub releases](https://github.com/dvbern/junit-beanvalidation-extension/releases)
or [Maven Central](https://search.maven.org/search?q=g:ch.dvbern.oss.junit-beanvalidation-extension%20a:junit-beanvalidation-extension)

## Basic Usage

... and that is also all there is to it.

```java
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

```

## Common Use-Cases

This extension comes in quite handy if you have ConstraintValidators that get injected some parameters, e.g.:

```java
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

```

Integrating the standard BeanValidation ValidatorFactory makes using such Validators quite a pain.

Using the method described in [Basic Usage](#basic-usage), you now can create your ConstraintValidator instances easily
in your test setup using Mocks/Stubs/Fakes/whatever.

## Nullability

All parameters/returns are Non-Null if not explicitly stated by a @Nullable annotation!

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Contributing Guidelines

Please read [CONTRIBUTING.md] for the process for submitting pull requests to us.

## Code of Conduct

One healthy social atmospehere is very important to us, wherefore we rate our Code of Conduct high. For details check
the file [CODE_OF_CONDUCT.md]

## Authors

* **DV Bern AG** - *Initial work* - [dvbern](https://github.com/dvbern)

See also the list of [contributors](https://github.com/dvbern/junit-beanvalidation-extension/contributors) who
participated in this project.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE] file for details.

