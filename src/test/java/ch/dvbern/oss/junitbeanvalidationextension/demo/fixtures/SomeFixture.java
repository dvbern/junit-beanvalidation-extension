package ch.dvbern.oss.junitbeanvalidationextension.demo.fixtures;

import jakarta.validation.constraints.NotNull;

@CustomConstraint
public class SomeFixture {
	@NotNull
	private String foo;

	public SomeFixture(String foo) {
		this.foo = foo;
	}

	public String getFoo() {
		return foo;
	}

	public void setFoo(String foo) {
		this.foo = foo;
	}
}
