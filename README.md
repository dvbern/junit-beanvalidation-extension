# junit-beanvalidation-extension

Allow for easy injection (and optionally: customization) of a Java BeanValidation Validator into your tests.

This enables you to use ConstraintValidators that need special setup (like e.g. Mocks).

## Prerequisites/Dependencies

This project requires Java >= 11 due to JakartaEE.

To spare you mostly of dependency hell, this library *does not* include dependencies on any beanvalidation library!
You have to add these dependencies yourself, see [Installing](#Installing).

## Releases

Current version: see [GitHub releases](https://github.com/dvbern/junit-beanvalidation-extension/releases)
or [Maven Central](https://search.maven.org/search?q=g:ch.dvbern.oss.junit-beanvalidation-extension%20a:junit-beanvalidation-extension)

### Installing

Maven dependency:

```xml

<dependency>
	<groupId>ch.dvbern.oss.junit-beanvalidation-extension</groupId>
	<artifactId>junit-beanvalidation-extension</artifactId>
	<version>see-github-releases</version>
	<scope>test</scope>
</dependency>
```

For available versions: see [Releases](#releases)

If you do **not** run this library in a container, you might need to add at least these dependencies to make
beanvalidation work:

```xml

<dependencies>
	<dependency>
		<!-- validation API spec -->
		<groupId>jakarta.validation</groupId>
		<artifactId>jakarta.validation-api</artifactId>
		<version>3.0.2</version>
		<scope>provided</scope>
	</dependency>
	<dependency>
		<!-- transitive requirement of the validtion API -->
		<groupId>org.glassfish</groupId>
		<artifactId>jakarta.el</artifactId>
		<version>4.0.2</version>
		<scope>test</scope>
	</dependency>
	<dependency>
		<!-- validation API implementation (hibernate-validaor is just used as an example) -->
		<groupId>org.hibernate.validator</groupId>
		<artifactId>hibernate-validator</artifactId>
		<version>8.0.0.Final</version>
		<scope>test</scope>
	</dependency>
</dependencies>
```

## Basic Usage

Just inject a `Validator` parameter into your test method... and that is basically all there is to it.

```java
// Enable the extension
@ExtendWith(ValidatorExtension.class)
public class CustomValidatorTest {
	@Test
	void validator_accepts_valid_input(Validator validator) {
		Set<ConstraintViolation<Object>> actual = validator.validate(new SomeFixture("Hello World"));

		assertThat(actual)
				.isEmpty();
	}
}

```

## Customization

Inject a `ValidatorCustomizer` into your setup methods (annotated by @BeforeAll/BeforeEach) and start customizing:

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

### Complex ConstraintValidator

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

### Other customizations

The `ValidatorCustomizer` allows modifying all properties of the
current [ValidationContext](https://docs.oracle.com/javaee/7/api/javax/validation/ValidatorContext.html)

## Nullability

All parameters/returns are Non-Null if not explicitly stated by
a [@Nullable](https://checkerframework.org/api/org/checkerframework/checker/nullness/qual/Nullable.html) annotation!

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

