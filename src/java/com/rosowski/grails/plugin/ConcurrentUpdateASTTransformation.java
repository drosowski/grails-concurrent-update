package com.rosowski.grails.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.List;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ConcurrentUpdateASTTransformation implements ASTTransformation {
// ========================================================================================================================
//    Static Fields
// ========================================================================================================================

    private static final Log LOG = LogFactory.getLog(ConcurrentUpdateASTTransformation.class);
    private static final String checkVersion = "version(validator: { Long value, object ->\n" +
            "      if (value != null) {\n" +
            "        if (object.getPersistentValue('version') > value) {\n" +
            "          return ['optimistic.locking.failure']\n" +
            "        }\n" +
            "      }\n" +
            "      return true\n" +
            "    })";


// ========================================================================================================================
//    Public Instance Methods
// ========================================================================================================================

    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        Expression checkVersion = getCheckVersion();
        if (checkVersion != null) {
            for (ASTNode astNode : astNodes) {
                if (astNode instanceof ClassNode) {
                    ClassNode classNode = (ClassNode) astNode;
                    addVersionConstraint(classNode, checkVersion);
                }
            }
        } else {
            LOG.warn("[OptimisticLockASTTransformation] Failed, template checkVersion could not be read.");
        }
    }

    private Expression getCheckVersion() {
        List<ASTNode> nodes = new AstBuilder().buildFromString(checkVersion);
        if (nodes != null && nodes.size() > 0) {
            BlockStatement block = (BlockStatement) nodes.get(0);
            List<Statement> statements = block.getStatements();
            if (statements != null && statements.size() > 0) {
                ReturnStatement returnStatement = (ReturnStatement) statements.get(0);
                return returnStatement.getExpression();
            }
        }
        return null;
    }

    private void addVersionConstraint(ClassNode classNode, Expression checkVersion) {
        FieldNode constraints = classNode.getField("constraints");
        ClosureExpression closure = (ClosureExpression) constraints.getInitialExpression();
        BlockStatement block = (BlockStatement) closure.getCode();
        block.addStatement(new ExpressionStatement(checkVersion));
        LOG.info("[OptimisticLockASTTransformation] Added constraint [version] to class [" + classNode.getName() + "]");
    }
}
