package ch.dvbern.oss.junitbeanvalidationextension;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static java.util.Objects.requireNonNull;

/**
 * Allows injection of a BeanValidation {@link Validator} as parameter into your test methods.
 * <p>
 * See also {@link ValidatorCustomizer} and {@link Customizations} for more details
 * on how to customize the Validator (e.g. use specific/custom validators).
 * </p>
 *
 * Typical usage:
 * <pre>{@code
 * @BeforeEach
 * public void beforeEach(
 * ValidatorCustomizer customizer
 * ) {
 * customizer.customize(Customizations.usingConstraintValidator(new MyFancyValidator(someMock)));
 * }
 *
 * @Test
 * void accepts_a_valid_bean(Validator validator) {
 * Set<ConstraiontViolation<FooFixture>> violations = validator.validate(new FooFixture());
 *
 * // assert on violations and so forth.
 * }
 * }</pre>
 */
public class ValidatorExtension
		implements ParameterResolver, BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {
	private static final Namespace NAMESPACE = Namespace.create(ValidatorExtension.class);
	private static final String DEFAULT_FACTORY = "factory";
	private static final String VALIDATOR = "validator";

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Class<?> type = parameterContext.getParameter().getType();
		boolean result = Validator.class.equals(type)
				|| ValidatorCustomizer.class.equals(type);

		return result;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		Class<?> typeName = parameterContext.getParameter().getType();
		InvocationOn invocationOn = determinInvocation(parameterContext);

		if (ValidatorCustomizer.class.equals(typeName)) {
			rejectTestMethod(invocationOn);
			return prepareCustomizer(extensionContext, invocationOn);
		}

		if (Validator.class.equals(typeName)) {
			rejectSetupMethods(invocationOn);
			return createValidator(extensionContext);
		}

		throw new IllegalArgumentException("Not supported: " + typeName);
	}

	private InvocationOn determinInvocation(ParameterContext parameterContext) {
		boolean isBeforeAll = parameterContext.getParameter().getDeclaringExecutable()
				.isAnnotationPresent(BeforeAll.class);
		if (isBeforeAll) {
			return InvocationOn.FOR_ALL;
		}

		boolean isBeforeEach = parameterContext.getParameter().getDeclaringExecutable()
				.isAnnotationPresent(BeforeEach.class);
		if (isBeforeEach) {
			return InvocationOn.FOR_EACH;
		}

		return InvocationOn.TEST_METHOD;
	}

	private void rejectSetupMethods(InvocationOn invocationOn) {
		if (invocationOn.isSetupMethod()) {
			throw new IllegalArgumentException(
					"Injecting the Validator into setup methods (@BeforeAll/BeforeEach) is not yet supported");
		}
	}

	private void rejectTestMethod(InvocationOn invocationOn) {
		if (invocationOn.isTestMethod()) {
			throw new IllegalArgumentException(
					String.format(
							"Injecting the %s into test methods is not supported",
							ValidatorCustomizer.class.getSimpleName()
					));
		}
	}

	private ValidatorCustomizerImpl prepareCustomizer(ExtensionContext context, InvocationOn where) {
		Store store = context.getStore(NAMESPACE);

		ValidatorCustomizerImpl customizer = new ValidatorCustomizerImpl();

		store.put(where.storeKey(), customizer);

		return customizer;
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		Store store = context.getStore(NAMESPACE);

		// building a completely new Validator from scratch is expensive, so just do it once in beforeAll
		ValidatorFactory defaultFactory = Validation.byDefaultProvider()
				.configure()
				.buildValidatorFactory();

		store.put(DEFAULT_FACTORY, defaultFactory);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		//		Store store = context.getStore(NAMESPACE);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		Store store = context.getStore(NAMESPACE);

		store.remove(InvocationOn.FOR_ALL.storeKey());
	}

	@Override
	public void afterAll(ExtensionContext context) {
		Store store = context.getStore(NAMESPACE);

		store.remove(InvocationOn.FOR_ALL.storeKey());
		store.remove(VALIDATOR);

		ValidatorFactory factory = (ValidatorFactory) store.get(DEFAULT_FACTORY);
		factory.close();
		store.remove(DEFAULT_FACTORY);

	}

	private Validator createValidator(ExtensionContext context) {
		Store store = context.getStore(NAMESPACE);

		ValidatorFactory factory = requireNonNull(
				(ValidatorFactory) store.get(DEFAULT_FACTORY),
				"ValidatorFactory instance not found in Test context store???"
		);

		ValidatorContext ctx = applyCustomizers(factory, store);

		Validator validator = ctx.getValidator();

		return validator;
	}

	private ValidatorContext applyCustomizers(ValidatorFactory factory, Store store) {
		ValidatorCustomizerImpl eachCustomizer = findCustomizer(InvocationOn.FOR_EACH.storeKey(), store);
		if (eachCustomizer != null) {
			return eachCustomizer.applyCustomizations(factory.usingContext(), factory);
		}

		ValidatorCustomizerImpl allCustomizer = findCustomizer(InvocationOn.FOR_ALL.storeKey(), store);
		if (allCustomizer != null) {
			return allCustomizer.applyCustomizations(factory.usingContext(), factory);
		}

		return factory.usingContext();
	}

	private @Nullable ValidatorCustomizerImpl findCustomizer(
			String where,
			Store store
	) {
		ValidatorCustomizerImpl result = (ValidatorCustomizerImpl) store.get(where);

		return result;
	}

}
