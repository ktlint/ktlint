package com.pinterest.ktlint.rule.engine.core.api

import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SCRIPT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SCRIPT_INITIALIZER
import com.pinterest.ktlint.rule.engine.core.util.cast
import org.jetbrains.kotlin.cli.jvm.compiler.IdeaStandaloneExecutionSetup
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironmentMode
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreProjectEnvironment
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.diagnostic.DefaultLogger
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.util.UserDataHolderBase
import org.jetbrains.kotlin.com.intellij.pom.PomModel
import org.jetbrains.kotlin.com.intellij.pom.PomModelAspect
import org.jetbrains.kotlin.com.intellij.pom.PomTransaction
import org.jetbrains.kotlin.com.intellij.pom.impl.PomTransactionBase
import org.jetbrains.kotlin.com.intellij.pom.tree.TreeAspect
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import sun.reflect.ReflectionFactory
import org.jetbrains.kotlin.com.intellij.openapi.diagnostic.Logger as DiagnosticLogger

private const val IDEA_HOME_PATH_PROPERTY = "idea.home.path"
private const val IDEA_CONFIG_PATH_PROPERTY = "idea.config.path"

/**
 * Embedded Kotlin Compiler configured for use by Ktlint.
 */
public object KtlintKotlinCompiler {
    private val psiFileFactory = initPsiFileFactory()

    /**
     * Create a PSI file with name [psiFileName] and content [text].
     */
    public fun createPsiFileFromText(
        psiFileName: String,
        text: String,
    ): PsiFile = psiFileFactory.createFileFromText(psiFileName, KotlinLanguage.INSTANCE, text)

    /**
     * Create the AST for a given piece of code.
     */
    public fun createASTNodeFromText(text: String): ASTNode? =
        // For a code snippet which is not necessarily compilable if it was compiled as a standalone file, it is better to compile it as
        // kotlin script.
        createPsiFileFromText("File.kts", text)
            .node
            .findChildByType(SCRIPT)
            ?.findChildByType(BLOCK)
            ?.let { it.findChildByType(SCRIPT_INITIALIZER) ?: it }
}

/**
 * Initialize Kotlin Lexer.
 */
private fun initPsiFileFactory(): PsiFileFactory {
    DiagnosticLogger.setFactory(LoggerFactory::class.java)

    val disposable = Disposer.newDisposable()
    // When running ktlint via the ktlint-intellij-plugin in IntelliJ IDEA, an exception is thrown whenever certain properties are not set
    // (https://github.com/nbadal/ktlint-intellij-plugin/issues/614). When initializing the embedded kotlin compiler while running it within
    // a process in IntelliJ IDEA those settings need to have a non-null value.
    val ideaHomePath = System.getProperties().setProperty(IDEA_HOME_PATH_PROPERTY, "/ktlint/$IDEA_HOME_PATH_PROPERTY") as String?
    val ideaConfigPath = System.getProperties().setProperty(IDEA_CONFIG_PATH_PROPERTY, "/ktlint/$IDEA_CONFIG_PATH_PROPERTY") as String?
    try {
        val kotlinCoreProjectEnvironment = createKotlinProjectEnvironment(disposable)
        return PsiFileFactory.getInstance(kotlinCoreProjectEnvironment.project)
    } catch (t: Throwable) {
        throw UnsupportedOperationException(t)
    } finally {
        // Dispose explicitly to (possibly) prevent memory leak
        // https://discuss.kotlinlang.org/t/memory-leak-in-kotlincoreenvironment-and-kotlintojvmbytecodecompiler/21950
        // https://youtrack.jetbrains.com/issue/KT-47044
        disposable.dispose()
        // Restore the system settings
        if (ideaHomePath == null) {
            System.clearProperty(IDEA_HOME_PATH_PROPERTY)
        } else {
            System.setProperty(IDEA_HOME_PATH_PROPERTY, ideaHomePath)
        }
        if (ideaConfigPath == null) {
            System.clearProperty(IDEA_CONFIG_PATH_PROPERTY)
        } else {
            System.setProperty(IDEA_CONFIG_PATH_PROPERTY, ideaConfigPath)
        }
    }
}

private fun createKotlinProjectEnvironment(disposable: Disposable): KotlinCoreProjectEnvironment {
    IdeaStandaloneExecutionSetup.doSetup()
    val kotlinCoreApplicationEnvironment =
        KotlinCoreApplicationEnvironment
            .create(
                disposable,
                KotlinCoreApplicationEnvironmentMode.Production,
            ).also {
                it.registerParserDefinition(KotlinParserDefinition())
                // Needed this as well to parse the .kt files
                it.registerFileType(KotlinFileType.INSTANCE, "kt")
            }
    return KotlinCoreProjectEnvironment(disposable, kotlinCoreApplicationEnvironment).also {
        it
            .project
            .cast<MockProject>()
            .apply { registerFormatPomModel() }
    }
}

/**
 * Do not print anything to the stderr when lexer is unable to match input.
 */
private class LoggerFactory : DiagnosticLogger.Factory {
    override fun getLoggerInstance(p: String): DiagnosticLogger =
        object : DefaultLogger(null) {
            override fun warn(
                message: String?,
                t: Throwable?,
            ) {}

            override fun error(
                message: String?,
                vararg details: String?,
            ) {}
        }
}

private fun MockProject.registerFormatPomModel() {
    registerService(PomModel::class.java, FormatPomModel())
}

private class FormatPomModel :
    UserDataHolderBase(),
    PomModel {
    override fun runTransaction(transaction: PomTransaction) {
        (transaction as PomTransactionBase).run()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : PomModelAspect> getModelAspect(aspect: Class<T>): T? {
        if (aspect == TreeAspect::class.java) {
            // using approach described in https://git.io/vKQTo due to the magical bytecode of TreeAspect
            // (check constructor signature and compare it to the source)
            // (org.jetbrains.kotlin:kotlin-compiler-embeddable:1.0.3)
            val constructor =
                ReflectionFactory
                    .getReflectionFactory()
                    .newConstructorForSerialization(
                        aspect,
                        Any::class.java.getDeclaredConstructor(*arrayOfNulls<Class<*>>(0)),
                    )
            return constructor.newInstance() as T
        }
        return null
    }
}
