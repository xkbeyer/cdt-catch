package org.eclipse.cdt.testsrunner.internal.catch_test.entities;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Catch test case result summary
 */
public class OverallResult {
	@XmlAttribute
	private boolean success;
	@XmlAttribute
	private double durationInSeconds;

	/**
	 * Indicates test case success status.
	 *
	 * @return <code>true</code> if test case status is success.
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Provides the execution of the associated test case.
	 *
	 * @return The associated test case's execution time.
	 */
	public double getDurationInSeconds() {
		return durationInSeconds;
	}
}
