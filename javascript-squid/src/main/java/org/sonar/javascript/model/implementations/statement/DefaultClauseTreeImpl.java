/*
 * SonarQube JavaScript Plugin
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
package org.sonar.javascript.model.implementations.statement;

import com.google.common.collect.Iterators;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import org.sonar.javascript.model.implementations.JavaScriptTree;
import org.sonar.javascript.model.implementations.lexical.InternalSyntaxToken;
import org.sonar.javascript.model.interfaces.Tree;
import org.sonar.javascript.model.interfaces.lexical.SyntaxToken;
import org.sonar.javascript.model.interfaces.statement.DefaultClauseTree;
import org.sonar.javascript.model.interfaces.statement.StatementTree;

import java.util.Iterator;
import java.util.List;

public class DefaultClauseTreeImpl extends JavaScriptTree implements DefaultClauseTree {

  private final SyntaxToken defaultKeyword;
  private final SyntaxToken colon;

  public DefaultClauseTreeImpl(InternalSyntaxToken defaultKeyword, InternalSyntaxToken colon) {
    super(Kind.DEFAULT_CLAUSE);
    this.defaultKeyword = defaultKeyword;
    this.colon = colon;

    addChildren(defaultKeyword, colon);
  }

  public DefaultClauseTreeImpl(InternalSyntaxToken defaultKeyword, InternalSyntaxToken colon, AstNode statementList) {
    super(Kind.DEFAULT_CLAUSE);
    this.defaultKeyword = defaultKeyword;
    this.colon = colon;

    addChildren(defaultKeyword, colon, statementList);
  }

  @Override
  public SyntaxToken keyword() {
    return defaultKeyword;
  }

  @Override
  public SyntaxToken colon() {
    return colon;
  }

  @Override
  public List<StatementTree> statements() {
    throw new UnsupportedOperationException("Not supported yet in the strongly typed AST.");
  }

  @Override
  public AstNodeType getKind() {
    return Kind.DEFAULT_CLAUSE;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.emptyIterator();
  }

}