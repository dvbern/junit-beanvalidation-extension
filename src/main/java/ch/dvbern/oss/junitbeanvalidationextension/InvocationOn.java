package ch.dvbern.oss.junitbeanvalidationextension;

enum InvocationOn {
	FOR_ALL(true),
	FOR_EACH(true),
	TEST_METHOD(false),
	;

	private final boolean setupMethod;

	InvocationOn(boolean setupMethod) {
		this.setupMethod = setupMethod;
	}

	public boolean isSetupMethod() {
		return setupMethod;
	}

	public boolean isTestMethod() {
		return !setupMethod;
	}

	public String storeKey() {
		return name();
	}
}
