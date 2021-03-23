package ch.dvbern.oss.junitbeanvalidationextension;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

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

import static java.lang.String.format;
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
	private static final String FOR_ALL_CUSTOMIZER = "for_all_validators";
	private static final String FOR_EACH_CUSTOMIZER = "for_each_validators";

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

		if (ValidatorCustomizer.class.equals(typeName)) {
			return resolveCustomValidators(extensionContext);
		}

		if (Validator.class.equals(typeName)) {
			rejectSetupMethods(parameterContext);
			return createValidator(extensionContext);
		}

		throw new IllegalArgumentException("Not supported: " + typeName);
	}

	private void rejectSetupMethods(ParameterContext parameterContext) {
		boolean isBeforeAll = parameterContext.getParameter().getDeclaringExecutable()
				.isAnnotationPresent(BeforeAll.class);
		boolean isBeforeEach = parameterContext.getParameter().getDeclaringExecutable()
				.isAnnotationPresent(BeforeEach.class);

		if (isBeforeAll || isBeforeEach) {
			throw new IllegalArgumentException(
					"Injecting the Validator into setup methods (@BeforeAll/BeforeEach) is nott yet supported");
		}

	}

	private Object resolveCustomValidators(ExtensionContext context) {
		@Nullable Object foundEach = resolveCustomValidators(context, FOR_EACH_CUSTOMIZER);
		if (foundEach != null) {
			return foundEach;
		}

		@Nullable Object foundAll = resolveCustomValidators(context, FOR_ALL_CUSTOMIZER);
		requireNonNull(foundAll, "Junit Extension not setup correctly???");

		return foundAll;
	}

	private @Nullable Object resolveCustomValidators(ExtensionContext context, String where) {
		Store store = context.getStore(NAMESPACE);
		Object result = store.get(where);

		return result;
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		Store store = context.getStore(NAMESPACE);

		// building a completely new Validator from scratch is expensive, so just do it once in beforeAll
		ValidatorFactory defaultFactory = Validation.byDefaultProvider()
				.configure()
				.buildValidatorFactory();

		store.put(DEFAULT_FACTORY, defaultFactory);
		store.put(FOR_ALL_CUSTOMIZER, new ValidatorCustomizerImpl());
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		Store store = context.getStore(NAMESPACE);

		store.put(FOR_EACH_CUSTOMIZER, new ValidatorCustomizerImpl());
	}

	@Override
	public void afterEach(ExtensionContext context) {
		Store store = context.getStore(NAMESPACE);

		store.remove(FOR_EACH_CUSTOMIZER);
	}

	@Override
	public void afterAll(ExtensionContext context) {
		Store store = context.getStore(NAMESPACE);

		store.remove(FOR_ALL_CUSTOMIZER);
		store.remove(VALIDATOR);

		ValidatorFactory factory = (ValidatorFactory) store.get(DEFAULT_FACTORY);
		factory.close();
		store.remove(DEFAULT_FACTORY);

	}

	private Validator createValidator(ExtensionContext context) {
		Store store = context.getStore(NAMESPACE);

		ValidatorFactory factory = requireNonNull(
				(ValidatorFactory) store.get(DEFAULT_FACTORY),
				"ValidatorFactory instance not found in Test context store???");

		ValidatorContext ctx = applyCustomizers(factory, store);

		Validator validator = ctx.getValidator();

		return validator;
	}

	private ValidatorContext applyCustomizers(ValidatorFactory factory, Store store) {

		ValidatorContext tmpCtx = findCustomizer(FOR_ALL_CUSTOMIZER, store)
				.applyCustomizations(factory.usingContext(), factory.getConstraintValidatorFactory());

		ValidatorContext result = findCustomizer(FOR_EACH_CUSTOMIZER, store)
				.applyCustomizations(tmpCtx, factory.getConstraintValidatorFactory());

		return result;
	}

	private ValidatorCustomizerImpl findCustomizer(
			String where,
			Store store
	) {
		ValidatorCustomizerImpl result = (ValidatorCustomizerImpl) store.get(where);
		requireNonNull(result, format("Could not find CustomValidators in store: %s", where));

		return result;
	}

}
