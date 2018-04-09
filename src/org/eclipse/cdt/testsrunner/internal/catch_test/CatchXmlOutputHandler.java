package org.eclipse.cdt.testsrunner.internal.catch_test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

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

public class CatchXmlOutputHandler {
   private ITestModelUpdater modelUpdater = null;
   private InputStream reader = null;
   
   private class TestCaseHolder {
      public String name = "";
      public String file = "N/A";
      public int line = 0;
      public Status outCome = Status.Passed;
      public String expression= "N/A";
      public String text= "";
   }

   private Stack<TestCaseHolder> testCases = new Stack<TestCaseHolder>();

   public CatchXmlOutputHandler(InputStream input, ITestModelUpdater modelUpdater) {
      this.modelUpdater = modelUpdater;
      this.reader = input;
   }

   public void run() throws IOException, TestingException, ParserConfigurationException, SAXException {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      DefaultHandler handler = new DefaultHandler() {
         public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
            if( qName.equals("Group") ) {
               modelUpdater.enterTestSuite(attr.getValue("name"));
            }
            if( qName.equals("TestCase") ) {
               TestCaseHolder currentCase = new TestCaseHolder();
               modelUpdater.enterTestCase(attr.getValue("name"));

               currentCase.name = attr.getValue("name");
               currentCase.file = attr.getValue("filename");
               currentCase.line = Integer.parseInt(attr.getValue("line"));
               testCases.push(currentCase);
            }
            if( qName.equals("Section") ) {
               TestCaseHolder currentCase = new TestCaseHolder();
               currentCase.name = testCases.peek().name +  "/" + attr.getValue("name");
               currentCase.file = attr.getValue("filename");
               currentCase.line = Integer.parseInt(attr.getValue("line"));
               modelUpdater.enterTestCase(currentCase.name);
               testCases.push(currentCase);
            }
            if( qName.equals("Expression") ) {
               TestCaseHolder currentCase = testCases.peek();
               currentCase.outCome = attr.getValue("success").equals("true") ? Status.Passed : Status.Failed;
               currentCase.expression = attr.getValue("type");
               currentCase.file = attr.getValue("filename");
               currentCase.line = Integer.parseInt(attr.getValue("line"));
            }
            if( qName.equals("OverallResult") ) {
               TestCaseHolder currentCase = testCases.peek();
               currentCase.outCome = attr.getValue("success").equals("true") ? Status.Passed : Status.Failed;
               Double timeInMilliseconds = Double.parseDouble(attr.getValue("durationInSeconds")) * 1000.;
               modelUpdater.setTestingTime(timeInMilliseconds.intValue());
            }
            if( qName.equals("Failure") ) {
               TestCaseHolder currentCase = testCases.peek();
               currentCase.outCome = Status.Failed;
               currentCase.file = attr.getValue("filename");
               currentCase.line = Integer.parseInt(attr.getValue("line"));
            }
         };

         public void endElement(String uri, String localName, String qName) throws SAXException {
            if( qName.equals("Group") ) {
               modelUpdater.exitTestSuite();
            } else if( qName.equals("Failure") ) {
               TestCaseHolder currentCase = testCases.peek();
               modelUpdater.addTestMessage(currentCase.file, currentCase.line, ITestMessage.Level.FatalError, currentCase.text);
               currentCase.text = "";
            } else if( qName.equals("TestCase") ) {
               TestCaseHolder currentCase = testCases.pop();
               modelUpdater.setTestStatus(currentCase.outCome);
               modelUpdater.exitTestCase();
            } else if( qName.equals("Section") ) {
               TestCaseHolder currentCase = testCases.pop();
               modelUpdater.setTestStatus(currentCase.outCome);
               modelUpdater.exitTestCase();
               modelUpdater.enterTestCase(testCases.peek().name);
            } else if( qName.equals("Original") ) {
               TestCaseHolder currentCase = testCases.peek();
               currentCase.expression += "(" + currentCase.text + ")";
               currentCase.text = "";
            } else if( qName.equals("Expanded") ) {
               TestCaseHolder currentCase = testCases.peek();
               currentCase.expression += " with expansion: (" + currentCase.text + ")";
               currentCase.text = "";
            } else if( qName.equals("Expression") ) {
               TestCaseHolder currentCase = testCases.peek();
               ITestMessage.Level lvl = currentCase.outCome == Status.Failed ? ITestMessage.Level.Error : ITestMessage.Level.Message;
               modelUpdater.addTestMessage(currentCase.file, currentCase.line, lvl, currentCase.expression);
               currentCase.expression = "";
            }
         };

         public void characters(char[] ch, int start, int length) throws SAXException {
            if( !testCases.empty() && length != 0 ) {
               TestCaseHolder currentCase = testCases.peek();
               currentCase.text += new String(ch, start, length).trim();
            }
         };
      };
      parser.parse(reader, handler);
   }
}
