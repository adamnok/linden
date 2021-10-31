package todoapp.util

import lindovo.FieldError
import lindovo.fieldvalidators.StringFieldValidators.*

class I18N:
  def apply(it: FieldError) =
    it match
      case IsRequire => "Required field"
      case MinTextLength(v) => s"Minimum $v character is required"
      case MaxTextLength(v) => s"Macimum $v character is allowed"
      case it =>
        it.message match
          case "create.todo.item.nameIsReserved" => "Todo item with this name already exists"
          case it => it