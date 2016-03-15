/* CatchOutputHandler - handles the catch console reporter output.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Klaus Beyer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.eclipse.cdt.testsrunner.internal.catch_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;

/**
 * 
 * Output handler for the catch console reporter
 *
 */
public class CatchOutputHandler {
	private final Pattern TILDE_PATTERN = Pattern.compile("^~{79}", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern VERSION_PATTERN = Pattern.compile(".+Catch\\s+v\\d+(\\.\\d+){2}.*", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern MINUS_PATTERN = Pattern.compile(".*-{79}", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern DOTS_PATTERN = Pattern.compile(".*\\.{79}", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern EQUAL_PATTERN = Pattern.compile(".*={2,79}", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern COMPL_DURATION_PATTERN = Pattern.compile("^Completed in (\\d+)(\\.\\d+)?(e-\\d+)?s", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$

	private ITestModelUpdater modelUpdater = null;
	private BufferedReader    reader = null;
	private String            line = "";
	private String            fileName = "";
	private String            testCaseName = "";
	private int               failedTest = 0;
	
	enum State { Init, TestCase, TestCaseResults };
	
	public CatchOutputHandler(BufferedReader input,ITestModelUpdater modelUpdater) {
		this.modelUpdater = modelUpdater;
		this.reader = input;
	}
	
	private boolean firstNonEmptyLine() throws IOException
	{
		while(line != null && line.isEmpty() ) {
			nextLine();
		}
		return line == null ? false : true;
	}

	private boolean nextNonEmptyLine() throws IOException
	{
		nextLine();
		while(line != null && line.isEmpty() ) {
			nextLine();
		}
		return line == null ? false : true;
	}

	private void nextLine() throws IOException
	{
		String newline = reader.readLine();
		if( newline != null )
			line = newline.replaceAll("\\[(\\d)(;)?(\\d+)?m", "");
		else 
			line = newline;
	}
	
	/**
	 * Search the header
	 * 
	 * The header has the following lines:
	 * <pre>
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * xxxxx ix a Catch vm.n.o host application
	 * Run with -? for options
	 * <p>
	 * @return false either the end of input is found or the tokens can't be matched. 
	 * @throws IOException
	 * @throws TestingException 
	 *
	 */
	private void searchHeader() throws IOException, TestingException
	{
		if( !firstNonEmptyLine() ) 
			throw new TestingException("Unexpected End of input stream.");
		
		Matcher m = TILDE_PATTERN.matcher(line);
		if (m.matches()) {
			nextLine();
			if ((m = VERSION_PATTERN.matcher(line)).matches()) {
				nextLine(); // Run with ...
				return;
			}
		}
		throw new TestingException("Failed to find the test header lines.");
	}
	
	/**
	 *  Search the test case header
	 * <pre>
	 * -----------------------------
	 * Test case name
	 * -----------------------------
	 * <p>
	 * @return false either the end of input is found or the tokens can't be matched. 
	 * @throws IOException 
	 * @throws TestingException 
	 *
	 */
	private void searchTestCase() throws IOException, TestingException
	{
		if( !firstNonEmptyLine() ) 
			throw new TestingException("Unexpected End of input stream.");

		Matcher m = MINUS_PATTERN.matcher(line);
		if (m.matches()) {
			nextLine();
			testCaseName = line;
			failedTest =  0;
			do {
				nextLine();
				m = MINUS_PATTERN.matcher(line);
				if (m.matches()) {
					searchTestCaseFileInfo();
					return;
				} 
			} while(line != null);
		}
		throw new TestingException("Failed to find the test case name lines.");
	}

	/**
	 *  Search the test case file location
	 * <pre>
	 * path/filename.ext:line:
	 * .............................
	 * <p>
	 * @return false if the tokens can't be matched. 
	 * @throws IOException 
	 *
	 */
	private boolean searchTestCaseFileInfo() throws IOException
	{
		nextLine();
		String[] fileAndLine = line.split(":");
		if( fileName != fileAndLine[0] ) {
			if( !fileName.isEmpty() )
				modelUpdater.exitTestSuite();
			fileName = fileAndLine[0];
			modelUpdater.enterTestSuite(fileName);
		}
		nextLine();
		Matcher m = DOTS_PATTERN.matcher(line);
		if (m.matches()) {
			modelUpdater.enterTestCase(testCaseName);
			return true;
		}
		return false;
	}
	
	private int toMilliseconds(String a, String b, String c)
	{
		String number = a;
		if( b != null )
			number += b;
		if( c != null )
			number += c;
		
		Double ms = Double.parseDouble(number) * 1000.;
		return ms.intValue();
	}

	private void parseTestCaseResults() throws IOException, TestingException
	{
		String[] fileAndLine = line.split(":");
		if (fileAndLine.length == 3) {
			if( fileAndLine[2].contains("FAILED")) {
				// FAILED
				failedTest++;
				modelUpdater.setTestStatus(Status.Failed);
				nextLine(); // assertion
				modelUpdater.addTestMessage(fileAndLine[0], Integer.parseInt(fileAndLine[1]), ITestMessage.Level.Error, line);
			} else {
				// As long as no failed test is detected set the passed attribute.
				// The model takes the last set as the global result. In order not
				// to have the test case as passed if one has failed avoid further set to passed.
				if( failedTest == 0 ) {
					modelUpdater.setTestStatus(Status.Passed);
				}
				nextLine(); // PASSED:
				nextLine(); // assertion
				modelUpdater.addTestMessage(fileAndLine[0], Integer.parseInt(fileAndLine[1]), ITestMessage.Level.Info, line);
			}
		} else {
			throw new TestingException("Unexpected input while parsing test case result.");
		}
		nextLine();
		if( !line.isEmpty() ) {
			// with expansion:
			// a == b 
			String extraline = line;
			nextLine();
			extraline += line;
			modelUpdater.addTestMessage(fileAndLine[0], Integer.parseInt(fileAndLine[1]), ITestMessage.Level.Info, extraline);
		}
	}
	
	public void run() throws IOException, TestingException
	{
		State state = State.Init;

		searchHeader();
		
		state = State.TestCase;
		
		while (nextNonEmptyLine()) {
			Matcher m;
			if ((m = EQUAL_PATTERN.matcher(line)).matches()) {
				ITestSuite suite = modelUpdater.currentTestSuite();
				if( suite != null) {
					modelUpdater.exitTestSuite();
				}
				return;
			}
			if ((m = COMPL_DURATION_PATTERN.matcher(line)).matches()) {
				// The format is s.ms
				int testTime = toMilliseconds(m.group(1), m.group(2), m.group(3)); 
				modelUpdater.setTestingTime(testTime); // Will have in milliseconds
				modelUpdater.exitTestCase();
				state = State.TestCase;
				continue;
			}
			switch( state ) {
				case TestCase:
					searchTestCase();
					state = State.TestCaseResults;
					break;
				case TestCaseResults:
					parseTestCaseResults();
					break;
				default:
					throw new TestingException("Unexpected input while parsing test case result.");
			}
		}
	}
}
