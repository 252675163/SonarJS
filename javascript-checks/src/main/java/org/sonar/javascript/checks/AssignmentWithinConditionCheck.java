/*
 * Sonar JavaScript Plugin
 * Copyright (C) 2011 SonarSource and Eriks Nukis
 * dev@sonar.codehaus.org
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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.squid.checks.SquidCheck;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.javascript.parser.EcmaScriptGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "AssignmentWithinCondition",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class AssignmentWithinConditionCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(
        EcmaScriptGrammar.IF_STATEMENT,
        EcmaScriptGrammar.DO_WHILE_STATEMENT,
        EcmaScriptGrammar.WHILE_STATEMENT,
        EcmaScriptGrammar.FOR_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode conditionNode = astNode.getFirstChild(EcmaScriptGrammar.CONDITION);
    if ((conditionNode != null) && (conditionNode.getChild(0).getFirstChild(EcmaScriptGrammar.ASSIGNMENT_EXPRESSION) != null)) {
      getContext().createLineViolation(this, "Remove this assignment from the expression.", conditionNode);
    }
  }

}
