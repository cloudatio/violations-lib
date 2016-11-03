package se.bjurr.violations.lib.parsers;

import static com.google.common.collect.Lists.newArrayList;
import static se.bjurr.violations.lib.model.SEVERITY.ERROR;
import static se.bjurr.violations.lib.model.SEVERITY.INFO;
import static se.bjurr.violations.lib.model.SEVERITY.WARN;
import static se.bjurr.violations.lib.model.Violation.violationBuilder;
import static se.bjurr.violations.lib.parsers.ViolationParserUtils.findAttribute;
import static se.bjurr.violations.lib.parsers.ViolationParserUtils.findIntegerAttribute;
import static se.bjurr.violations.lib.parsers.ViolationParserUtils.getAttribute;
import static se.bjurr.violations.lib.parsers.ViolationParserUtils.getChunks;
import static se.bjurr.violations.lib.reports.Reporter.CSSLINT;

import java.util.List;

import com.google.common.base.Optional;

import se.bjurr.violations.lib.model.SEVERITY;
import se.bjurr.violations.lib.model.Violation;

public class CSSLintParser implements ViolationsParser {

 @Override
 public List<Violation> parseFile(String string) throws Exception {
  List<Violation> violations = newArrayList();
  List<String> files = getChunks(string, "<file", "</file>");
  for (String fileChunk : files) {
   String filename = getAttribute(fileChunk, "name");
   List<String> issues = getChunks(fileChunk, "<issue", "/>");
   for (String issueChunk : issues) {
    Integer line = findIntegerAttribute(issueChunk, "line").or(1);
    Optional<Integer> charAttrib = findIntegerAttribute(issueChunk, "char");
    String severity = getAttribute(issueChunk, "severity");

    String message = getAttribute(issueChunk, "reason");
    String evidence = findAttribute(issueChunk, "evidence").or("").trim();
    violations.add(//
      violationBuilder()//
        .setReporter(CSSLINT)//
        .setStartLine(line)//
        .setColumn(charAttrib.orNull())//
        .setFile(filename)//
        .setSeverity(toSeverity(severity))//
        .setMessage(message + ": " + evidence)//
        .build()//
    );
   }
  }
  return violations;
 }

 public SEVERITY toSeverity(String severity) {
  if (severity.equalsIgnoreCase("ERROR")) {
   return ERROR;
  }
  if (severity.equalsIgnoreCase("WARNING")) {
   return WARN;
  }
  return INFO;
 }
}
