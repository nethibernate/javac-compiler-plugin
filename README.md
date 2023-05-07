# javac-compiler-plugin

### 如何编译和打包

在项目根目录下，使用如下代码进行编译：
```shell
javac -cp src --add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED -d . src/com/example/Unsafe.java src/com/example/MethodLoggingPlugin.java
```

编译完毕后，使用如下代码进行打包：
```shell
jar cf method-logging-plugin.jar com/* META-INF/services/com.sun.source.util.Plugin
```

打包之后的method-logging-plugin.jar就可以给其它项目做编译插件使用了。

---

### 使用方法：

在其它要编译的项目中，编译时指定classpath中加入上面的method-logging-plugin.jar包，并在编译的参数中增加：-Xplugin:MethodLogging即可。

举例：
在example目录中，有一个Test.java类，只要把method-logging-plugin.jar包copy到Test目录下，然后执行：
```shell
javac --class-path "./method-logging-plugin.jar" -Xplugin:MethodLogging Test.java
```
完毕后可以运行 ``java Test``看结果。

# 注意
该项目时依托于JDK 17建造的，如果是别的版本的JDK，可能会出现一些不确定的问题。
