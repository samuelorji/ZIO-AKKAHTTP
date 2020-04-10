package domain

sealed trait Todo
case class TodoName(name : String) extends Todo
case class TodoItem(id :Int , name :String) extends Todo

sealed trait TodoError extends Throwable

//describes errors such as id not found, id exists
case class ToDoItemError(errorMsg : String) extends TodoError

//descibes invalid query errors
case class QueryError(errorMsg : String) extends TodoError

//describes errors such as lost database connections
case class DbError(errorMsg : String) extends TodoError

