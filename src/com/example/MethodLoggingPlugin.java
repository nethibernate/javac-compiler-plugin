package com.example;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.*;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.parser.ParserFactory;

public class MethodLoggingPlugin implements Plugin {
	
	private Context context;
	private Log log = null;
	private Trees trees;
	
	private TreeMaker treeMaker;
	
	static {
		Unsafe.exportJdkModule();
	}
	
	@Override
	public String getName() {
		return "MethodLogging";
	}
	
	
	@Override
	public void init(JavacTask task, String... args) {
		context = ((BasicJavacTask) task).getContext();
		log = Log.instance(context);
		trees = Trees.instance(task);
		treeMaker = TreeMaker.instance(context);
		log.printRawLines("Init");
		task.addTaskListener(new TaskListener() {
			@Override
			public void finished(TaskEvent e) {
				if (e.getKind() == TaskEvent.Kind.PARSE) {
					e.getCompilationUnit().accept(new TreeScanner(){
						@Override
						public Object visitMethod(MethodTree node, Object o) {
							processMethod(node);
							return super.visitMethod(node, o);
						}
					}, null);
				}
			}
		});
	}
	
	public void processMethod(MethodTree methodTree) {
		
		
		var tree = (JCTree.JCMethodDecl) methodTree;
		String codeSnippet = """
				for (int i = 0; i < 3; i++) {
							System.out.println("Inject i: " + i);
						}
				""";
		ParserFactory parserFactory = ParserFactory.instance(context);
		JavacParser javacParser = parserFactory.newParser(codeSnippet, false, false, false);
		JCTree.JCStatement jcStatement = javacParser.parseStatement();
		
		tree.body.stats = tree.body.stats.prepend(jcStatement);
	}
	
	private static class MyScanner extends TreeScanner{
		
		private final Log log;
		private final Context context;
		private final Trees trees;
		private int i = 1;
		
		private MyScanner(Log log, Context context, Trees trees) {
			this.log = log;
			this.context = context;
			this.trees = trees;
		}
		
		@Override
		public Object visitMethod(MethodTree methodTree, Object o) {
					if (methodTree.getModifiers().getFlags().contains(javax.lang.model.element.Modifier.PUBLIC)) {
				injectCustomCode(methodTree, context);
			}
			return super.visitMethod(methodTree, o);
		}
		
		private void injectCustomCode(MethodTree method, Context context) {
			TreeMaker treeMaker = TreeMaker.instance(context);
			var names = Names.instance(context);
			// Create custom code to be injected
			JCTree.JCExpressionStatement customCode = treeMaker.Exec(
					treeMaker.Apply(
							List.nil(),
							treeMaker.Select(
									treeMaker.Select(
											treeMaker.Ident(names.fromString("System")),
											names.fromString("out")),
									names.fromString("println")),
							List.of(treeMaker.Literal("Custom code executed, i = " + i++))
					)
			);
			
			// Inject custom code at the beginning of the method
			JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) method;
			methodDecl.body.stats = methodDecl.body.stats.prepend(customCode);
			
			// Print the modified method for debugging purposes
			log.printRawLines("Modified method: " + methodDecl);
		}
		
	}
}