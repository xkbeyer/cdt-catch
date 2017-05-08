package org.eclipse.cdt.testsrunner.internal.catch_test.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Catch test suite node.
 */
public class Group {
	@XmlElement(name = "TestCase")
	private final List<TestCase> testCases = new ArrayList<>();
	@XmlAttribute
	private String name;

	/**
	 * Provides <code>this</code> suite's {@link TestCase test cases}.
	 *
	 * @return Immutable view of <code>this</code> suite's {@link TestCase test
	 *         cases}.
	 */
	public List<TestCase> getTestCases() {
		return Collections.unmodifiableList(testCases);
	}

	/**
	 * Test suite name.
	 *
	 * @return Catch test suite name.
	 */
	public String getName() {
		return name;
	}
}
