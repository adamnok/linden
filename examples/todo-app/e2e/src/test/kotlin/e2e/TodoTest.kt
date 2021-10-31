package e2e

import e2e.config.Config
import e2e.helper.TodoItems
import org.dongoteam.pendler.envs.dfa.DFAConfig
import org.dongoteam.pendler.envs.dfa.DFAEngine
import org.dongoteam.pendler.envs.dfa.DFATest
import org.dongoteam.pendler.envs.dfa.console.ConsoleEnvironment

fun main() {
    DFAEngine(
        mapOf(
            "url" to "http://localhost:8080"
        ),
        ConsoleEnvironment()
    ).start<TodoTest>(false)
}

/** metadesc
 *
 * @Name Add+Delete
 *
 * # Add and Delete actions
 *
 * In this test we are testing the **add a new todo item** and **delete a todo item** functions.
 */
class TodoTest : DFATest({

    /** metadoc
     *
     * @Name Initial validating
     *
     * Should be:
     *  - Todo input form is empty
     *  - `#ScoreForReady` contains zero value
     *  - `#ScoreForUnReady` contains zero value
     *  - We do not have todo items
     */
    task {
        web {
            expected("") equals id("TodoName")
            expected("0") equals id("ScoreForReady")
            expected("0") equals id("ScoreForUnReady")
        }
        code {
            assertEquals(0, TodoItems().size)
        }
    }


    /** metadoc
     * @Name One item
     *
     * Add an item after than delete
     */
    task {
        val name = Var {
            "My first todo item"
        }
        web {
            typing(name) on id("TodoName")
            typing("\n") on id("TodoName")
        }
        code {
            assertEquals(1, TodoItems().size)
            assertEquals(name,  TodoItems()[0].title)
        }
        code {
            TodoItems()[0].delete()
            assertEquals(0, TodoItems().size)
        }
    }
    
}) {
    override val config: DFAConfig
        get() = Config()
}