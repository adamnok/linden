package e2e.helper

import org.dongoteam.pendler.ego.*
import org.dongoteam.pendler.envs.dfa.common.components.CodeBlock
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import kotlin.reflect.KClass
import e2e.util.Selectors.className

@Suppress("TestFunctionName")
fun CodeBlock.TodoItems() = page<TodoItems>(id("TodoList"))

class TodoItems(
    base: Base,
    private val egoPage: EgoPage,
    private val webElement: WebElement
) : EgoPageItem(base) {

    private inline val containerWebElement: WebElement
        get() = webElement

    val size: Int
        get() = items().size

    fun items() = finds<TodoItem>(className("TodoBlock"))

    operator fun get(index: Int) = items()[index]
}