/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011 SonarSource and Eriks Nukis
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.javascript.checks;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.javascript.tree.impl.JavaScriptTree;
import org.sonar.plugins.javascript.api.tree.ModuleTree;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.statement.BlockTree;
import org.sonar.plugins.javascript.api.tree.statement.IfStatementTree;
import org.sonar.plugins.javascript.api.tree.statement.IterationStatementTree;
import org.sonar.plugins.javascript.api.visitors.BaseTreeVisitor;
import org.sonar.plugins.javascript.api.visitors.IssueLocation;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = "S2681",
  name = "Multiline blocks should be enclosed in curly braces",
  priority = Priority.CRITICAL,
  tags = {Tags.BUG, Tags.CWE})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
public class MultilineBlockCurlyBraceCheck extends BaseTreeVisitor {

  private static final Kind[] NESTING_STATEMENT_KINDS = {
    Kind.IF_STATEMENT,
    Kind.FOR_STATEMENT,
    Kind.FOR_IN_STATEMENT,
    Kind.FOR_OF_STATEMENT,
    Kind.WHILE_STATEMENT
  };

  public static final String IF_PRIMARY_MESSAGE = "This line will not be executed conditionally; "
    + "only the first line of this %s-line block will be. The rest will execute unconditionally.";
  public static final String LOOP_PRIMARY_MESSAGE = "This line will not be executed in a loop; "
    + "only the first line of this %s-line block will be. The rest will execute only once.";
  public static final String IF_SECONDARY_MESSAGE = "not conditionally executed";
  public static final String LOOP_SECONDARY_MESSAGE = "not executed in a loop";

  @Override
  public void visitModule(ModuleTree module) {
    checkStatements(module.items());
    super.visitModule(module);
  }

  @Override
  public void visitBlock(BlockTree tree) {
    checkStatements(tree.statements());
    super.visitBlock(tree);
  }

  private void checkStatements(List<? extends Tree> statements) {
    Tree previous = null;
    int statementIndex = 0;
    for (Tree statement : statements) {
      if (previous != null && previous.is(NESTING_STATEMENT_KINDS)) {
        Tree nestingStatement = previous;
        Tree nestedStatement = nestedStatement(nestingStatement);
        if (!nestedStatement.is(Kind.BLOCK)
          && column(nestedStatement) == column(statement)
          && column(nestingStatement) < column(statement)
        ) {
          addIssue(statement, nestingStatement, statements.subList(statementIndex + 1, statements.size()));
        }
      }
      previous = statement;
      statementIndex++;
    }
  }

  private static Tree nestedStatement(Tree nestingStatement) {
    if (nestingStatement.is(Kind.IF_STATEMENT)) {
      return ((IfStatementTree) nestingStatement).statement();
    }
    return ((IterationStatementTree) nestingStatement).statement();
  }

  private void addIssue(Tree statement, Tree nestingStatement, List<? extends Tree> otherStatements) {
    String primaryMessage = LOOP_PRIMARY_MESSAGE;
    String secondaryMessage = LOOP_SECONDARY_MESSAGE;
    if (nestingStatement.is(Kind.IF_STATEMENT)) {
      primaryMessage = IF_PRIMARY_MESSAGE;
      secondaryMessage = IF_SECONDARY_MESSAGE;
    }

    Tree firstStatementInPseudoBlock = nestedStatement(nestingStatement);
    Tree lastStatementInPseudoBlock = statement;
    List<IssueLocation> secondaryLocations = new ArrayList<>();
    for (Tree otherStatement : otherStatements) {
      if (column(otherStatement) != column(statement)) {
        break;
      }
      secondaryLocations.add(new IssueLocation(otherStatement, secondaryMessage));
      lastStatementInPseudoBlock = otherStatement;
    }

    int nbLines = line(lastStatementInPseudoBlock) - line(firstStatementInPseudoBlock) + 1;
    IssueLocation location = new IssueLocation(statement, String.format(primaryMessage, nbLines));
    getContext().addIssue(this, location, secondaryLocations, null);
  }

  private static int column(Tree tree) {
    return ((JavaScriptTree) tree).getFirstToken().column();
  }

  private static int line(Tree tree) {
    return ((JavaScriptTree) tree).getLine();
  }

}
