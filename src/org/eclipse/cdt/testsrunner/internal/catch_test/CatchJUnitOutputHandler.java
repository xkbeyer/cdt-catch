package org.eclipse.cdt.testsrunner.internal.catch_test;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CatchJUnitOutputHandler {
   private ITestModelUpdater modelUpdater = null;
   private InputStream reader = null;

   private class TestCaseHolder {
      public String file = "N/A";
      public int line = 0;
      public Status outCome = Status.Passed;
      public String expression= "N/A";
      public String text= "";
      public String message= "N/A";
   }

   private TestCaseHolder currentCase = null;
   
   public CatchJUnitOutputHandler(InputStream input, ITestModelUpdater modelUpdater) {
      this.modelUpdater = modelUpdater;
      this.reader = input;
   }

   public void run() throws IOException, TestingException {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      try {
         SAXParser parser = factory.newSAXParser();
         DefaultHandler handler = new DefaultHandler() {
            public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
               if( qName.equals("testsuite") ) {
                  modelUpdater.enterTestSuite(attr.getValue("name"));
               }
               if( qName.equals("testcase") ) {
                  modelUpdater.enterTestCase(attr.getValue("name"));
                  currentCase = new TestCaseHolder();
                  Double timeInMilliseconds =Double.parseDouble(attr.getValue("time")) * 1000.;
                  modelUpdater.setTestingTime(timeInMilliseconds.intValue());
               }
               if( qName.equals("failure") ) {
                  currentCase.outCome = Status.Failed;
                  currentCase.message = attr.getValue("message");
                  currentCase.expression = attr.getValue("type");
               }
            };

            public void endElement(String uri, String localName, String qName) throws SAXException {
               if( qName.equals("failure") ) {
                  if( currentCase.expression .equals("FAIL") ) {
                     String [] msg = currentCase.text.split("at");
                     currentCase.message = msg[0].trim();
                     currentCase.text = msg[1];
                  } else {
                     currentCase.text = currentCase.text.substring(3);
                  }
                  String [] fileAndLine = currentCase.text.split(":");
                  currentCase.line = Integer.parseInt(fileAndLine[1]);
                  currentCase.file = fileAndLine[0].trim();
                  String msg = currentCase.expression + "(" + currentCase.message + ")"; 
                  modelUpdater.addTestMessage(currentCase.file, currentCase.line, ITestMessage.Level.Error, msg);
                  currentCase.text = "";
               } else if( qName.equals("testcase") ) {
                  modelUpdater.setTestStatus(currentCase.outCome);
                  modelUpdater.exitTestCase();
               } else if( qName.equals("testsuite") ) {
                  modelUpdater.exitTestSuite();
               }
            };

            public void characters(char[] ch, int start, int length) throws SAXException {
               if(length != 0 && currentCase != null) {
                  currentCase.text += new String(ch, start, length).trim();
               }
            };
         };
         parser.parse(reader, handler);
      } catch(ParserConfigurationException e) {
         throw new TestingException(e.getMessage());
      } catch(SAXException e) {
         throw new TestingException(e.getMessage());
      }
   }

}
