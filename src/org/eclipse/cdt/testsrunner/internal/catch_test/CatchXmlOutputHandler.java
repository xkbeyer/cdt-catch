package org.eclipse.cdt.testsrunner.internal.catch_test;

import java.io.InputStream;
import java.text.MessageFormat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.cdt.testsrunner.internal.catch_test.entities.Catch;
import org.eclipse.cdt.testsrunner.internal.catch_test.entities.Expression;
import org.eclipse.cdt.testsrunner.internal.catch_test.entities.Failure;
import org.eclipse.cdt.testsrunner.internal.catch_test.entities.Group;
import org.eclipse.cdt.testsrunner.internal.catch_test.entities.TestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;

/**
 * Output handler for the Catch XML output handler.
 */
public class CatchXmlOutputHandler {
	private static final String MESSAGE_FORMAT = "{0} [{1}]";

	private static int toMilliseconds(final double durationInSeconds) {
		return (int) Math.round(durationInSeconds * 1000.0);
	}

	/**
	 * Parses the provided Catch XML input and updates {@link ITestModelUpdater
	 * modelUpdater}.
	 *
	 * @param is
	 *            Catch XML input
	 * @param modelUpdater
	 *            {@link ITestModelUpdater} to notify
	 * @throws JAXBException
	 *             if any XML parser error occurs
	 */
	public static void run(final InputStream is, final ITestModelUpdater modelUpdater) throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(Catch.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final Catch root = unmarshaller.unmarshal(new StreamSource(is), Catch.class).getValue();
		for (final Group group : root.getGroups()) {
			modelUpdater.enterTestSuite(group.getName());
			for (final TestCase testCase : group.getTestCases()) {
				modelUpdater.enterTestCase(testCase.getName());
				if (testCase.getResult().isSuccess()) {
					modelUpdater.setTestStatus(Status.Passed);
				} else {
					if (testCase.hasExpression()) {
						final Expression expression = testCase.getExpression();
						final String message = MessageFormat.format(MESSAGE_FORMAT, expression.getExpanded().trim(), expression.getOriginal().trim());
						modelUpdater.addTestMessage(expression.getFilename(), expression.getLine(), Level.Error, message);
					} else {
						final Failure failure = testCase.getFailure();
						modelUpdater.addTestMessage(failure.getFilename(), failure.getLine(), Level.Error, failure.getMessage().trim());
					}
					modelUpdater.setTestStatus(Status.Failed);
				}
				modelUpdater.setTestingTime(toMilliseconds(testCase.getResult().getDurationInSeconds()));
				modelUpdater.exitTestCase();
			}
			modelUpdater.exitTestSuite();
		}
	}
}
