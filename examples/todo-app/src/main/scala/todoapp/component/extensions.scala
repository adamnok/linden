package todoapp.component

import todoapp.form.CreateTodoItem
import todoapp.form.CreateTodoItemW
import lindovo.StoreCollectForForm

extension (it: StoreCollectForForm[CreateTodoItem, CreateTodoItemW])
  inline def name = it.store.name
  inline def $name = it.formValidatedStore.name
