package db

import domain.{TodoError, TodoItem, TodoName}
import zio.IO

object Database {
  trait Service {
    def create(todo : TodoName) : IO[TodoError,Unit]
    def findTodo(id : Int) : IO[TodoError, TodoItem]
    def updateTodo(id : Int, todo : TodoName) : IO[TodoError, Unit]
    def deleteTodo(id : Int) : IO[TodoError,Unit]
    def getAll: IO[TodoError,List[TodoItem]]
  }
}
trait Database {
  def database: Database.Service
}
