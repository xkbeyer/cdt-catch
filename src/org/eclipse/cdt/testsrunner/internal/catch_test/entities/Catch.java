package org.eclipse.cdt.testsrunner.internal.catch_test.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * Catch root xml node.
 */
public class Catch {
	@XmlElement(name = "Group")
	private final List<Group> groups = new ArrayList<>();

	/**
	 * Each test suite is a group.
	 *
	 * @return Catch test suites.
	 */
	public List<Group> getGroups() {
		return Collections.unmodifiableList(groups);
	}
}
