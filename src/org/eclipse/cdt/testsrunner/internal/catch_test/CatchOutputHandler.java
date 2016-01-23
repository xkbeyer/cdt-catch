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
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;

/**
 * 
 * Output handler for the catch console reporter
 *
 */
public class CatchOutputHandler {
	private final Pattern TILDE_PATTERN = Pattern.compile("^~*", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern VERSION_PATTERN = Pattern.compile(".+Catch\\s+v\\d+(\\.\\d+){2}.*", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern MINUS_PATTERN = Pattern.compile("^-*", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern DOTS_PATTERN = Pattern.compile("^\\.*", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern EQUAL_PATTERN = Pattern.compile("^=*", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern COMPL_DURATION_PATTERN = Pattern.compile("^Completed in (\\d+)(\\.\\d+)?(e-\\d+)?s", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$

	private ITestModelUpdater modelUpdater = null;
	private BufferedReader    reader = null;
	
	public CatchOutputHandler(BufferedReader input,ITestModelUpdater modelUpdater) {
		this.modelUpdater = modelUpdater;
		this.reader = input;
	}
	
	/**
	 *  Search the header
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * xxxxx ix a Catch vm.n.o host application
	 * Run with ....
	 * @throws IOException 
	 *
	 */
	private boolean searchHeader(String line) throws IOException
	{
		int state = 0 ;
		do {
			if( line.isEmpty() ) 
				continue;
			Matcher m;
			if (state == 0) {
				if ((m = TILDE_PATTERN.matcher(line)).matches()) {
					state = 1;
				}
			} else if ((m = VERSION_PATTERN.matcher(line)).matches()) {
				return true;
			}
		} while( (line = reader.readLine()) != null );
		return false;
	}
	
	/**
	 *  Search the test case header
	 * -----------------------------
	 * Test case name
	 * -----------------------------
	 * @throws IOException 
	 *
	 */
	private boolean searchTestCase(String line) throws IOException
	{
		int state = 0 ;
		do {
			if( line.isEmpty() ) 
				continue;
			Matcher m;
			if ((m = MINUS_PATTERN.matcher(line)).matches()) {
				if( state == 0 ) {
					line = reader.readLine();
					modelUpdater.enterTestCase(line);
					state = 1;
				} else if( state == 1 ) {
					return true;
				}
			}
		} while( (line = reader.readLine()) != null );
		return false;
	}

	/**
	 *  Search the test case file location
	 * 
	 * path/filename.ext:line:
	 * .............................
	 * @throws IOException 
	 *
	 */
	private boolean searchTestCaseFileInfo(String line) throws IOException
	{
		String[] fileAndLine = line.split(":");
		modelUpdater.addTestMessage(fileAndLine[0], Integer.parseInt(fileAndLine[1]), ITestMessage.Level.Message, "Start of test case.");
		line = reader.readLine();
		Matcher m;
		if ((m = DOTS_PATTERN.matcher(line)).matches()) {
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

	public void run() throws IOException
	{
		String line;
		int state = 0;
		String[] fileAndLine = null;
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty())
				continue;
			Matcher m;
			if ((m = EQUAL_PATTERN.matcher(line)).matches()) {
				//modelUpdater.addTestMessage(null, 0, ITestMessage.Level.Message, "Finished.");
				return;
			}
			if (state == 0) {
				if (searchHeader(line)) {
					state = 1;
				}
				continue;
			}
			if (state == 1) {
				if( searchTestCase(line) ) {
					state = 2;
				}
				continue;
			}
			if (state == 2) {
				if( searchTestCaseFileInfo(line) ) {
					state = 3;
				}
				continue;
			}
			if ((m = COMPL_DURATION_PATTERN.matcher(line)).matches()) {
				// The format is s.ms
				int testTime = toMilliseconds(m.group(1), m.group(2), m.group(3)); 
				modelUpdater.setTestingTime(testTime); // Will have in milliseconds
				modelUpdater.exitTestCase();
				state = 1;
			}
			if (state == 3) {
				fileAndLine = line.split(":");
				if (fileAndLine.length == 3) {
					if( fileAndLine[2].contains("FAILED")) {
						// FAILED
						modelUpdater.setTestStatus(Status.Failed);
						line = reader.readLine(); // assertion
						modelUpdater.addTestMessage(fileAndLine[0], Integer.parseInt(fileAndLine[1]), ITestMessage.Level.Error, line);
					} else {
						modelUpdater.setTestStatus(Status.Passed);
						line = reader.readLine(); // PASSED:
						line = reader.readLine(); // assertion
						modelUpdater.addTestMessage(fileAndLine[0], Integer.parseInt(fileAndLine[1]), ITestMessage.Level.Info, line);
					}
				} else {
					// TODO Error handling
				}
				line = reader.readLine();
				if( !line.isEmpty() ) {
					// with expansion:
					// a == b 
					String extraline = line + reader.readLine();
					modelUpdater.addTestMessage(fileAndLine[0], Integer.parseInt(fileAndLine[1]), ITestMessage.Level.Info, extraline);
				}
				continue;
			}
		}
	}
}
