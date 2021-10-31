package e2e.helper

import org.dongoteam.pendler.ego.*
import org.dongoteam.pendler.ego.items.Clickable
import org.dongoteam.pendler.ego.items.Getter
import org.openqa.selenium.WebElement
import e2e.util.Selectors.className

class TodoItem(base: Base) : EgoPageItem(base) {
    val title: String
        get() =
            find<Getter>(className("TodoItemName")).text()

    fun delete() {
        find<Clickable>(className("DeleteItem")).click()
    }
}