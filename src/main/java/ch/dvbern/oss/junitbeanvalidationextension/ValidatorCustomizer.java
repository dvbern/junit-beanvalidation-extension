package ch.dvbern.oss.junitbeanvalidationextension;

/**
 * You may inject this parameter in your {@link @BeforeAll} test setup.
 *
 * Customizations in beforeEach <b>replace</b> from beforeAll
 * for all test-method-invocations in the applicable test class.
 *
 * Example usage:
 *
 * <pre>{@code
 *  @BeforeEach
 *  public void beforeEach(
 * 			ValidatorCustomizer customizer
 * 	) {
 * 		customizer.customize(Customizations.usingConstraintValidator(new MyCustomConstraintValidator("Special Setup")));
 *    }
 * }</pre>
 */
public interface ValidatorCustomizer {
	void customize(ContextCustomizer customizer);
}
