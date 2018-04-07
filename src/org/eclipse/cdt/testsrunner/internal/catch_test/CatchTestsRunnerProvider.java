/* CatchOutputHandler - handles the catch reporter output.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.xml.sax.SAXException;

/**
 * The Tests Runner provider plug-in to run tests with Catch.Test framework.
 * 
 */
public class CatchTestsRunnerProvider implements ITestsRunnerProvider {

   static private File tempOutFileName = null; 

   @Override
   public String[] getAdditionalLaunchParameters(String[][] testPaths) throws TestingException {
      final String[] catchParameters = { 
            "--success",        //$NON-NLS-1$
            "--reporter junit", //$NON-NLS-1$
            "--durations yes",  //$NON-NLS-1$
            "--out ./out.txt"   //$NON-NLS-1$
      };
      
      // Build tests filter
      if(testPaths != null && testPaths.length != 0) {
         throw new TestingException(CatchTestsRunnerMessages.CatchTestsRunner_wrong_tests_paths_count);
      }
      try {
         tempOutFileName = File.createTempFile("catch-", ".out");
         catchParameters[3] = "--out " + tempOutFileName;
      } catch(IOException e) {
         throw new TestingException(e.getMessage());
      }
      return catchParameters;
   }

   /**
    * Construct the error message from prefix and detailed description.
    *
    * @param prefix
    *           prefix
    * @param description
    *           detailed description
    * @return the full message
    */
   private String getErrorText(String prefix, String description) {
      return MessageFormat.format(CatchTestsRunnerMessages.CatchTestsRunner_error_format, prefix, description);
   }

   @Override
   public void run(ITestModelUpdater modelUpdater, InputStream inputStream) throws TestingException {
      try {
         InputStream input = Files.newInputStream(tempOutFileName.toPath(), StandardOpenOption.READ);
         CatchJUnitOutputHandler handler = new CatchJUnitOutputHandler(input, modelUpdater);
         handler.run();
      } catch(IOException e) {
         throw new TestingException(getErrorText(CatchTestsRunnerMessages.CatchTestsRunner_io_error_prefix, e.getMessage()));
      } catch(ParserConfigurationException e) {
         throw new TestingException(e.getMessage());
      } catch(SAXException e) {
         throw new TestingException(e.getMessage());
      } finally {
         tempOutFileName.delete();
         tempOutFileName = null;
      }
   }
}
