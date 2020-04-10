package controller

import db.Database
import domain._
import zio.{IO, ZIO}

object ApplicationController {

  def addTodo(todo : TodoName) : ZIO[Database, TodoError, Unit] = {
   ZIO.accessM[Database](_.database.create(todo))
  }

  def findTodoById(id : Int): ZIO[Database, TodoError, TodoItem] = {

    // a more verbose way ):
    val effectfullyAccessDatabase =  ZIO.accessM[Database]

    val useDatabaseToFindTodo : Database => IO[TodoError,TodoItem] =
      db => db.database.findTodo(id)

    effectfullyAccessDatabase.apply(useDatabaseToFindTodo)

  }

  def updateTodoById(id : Int,newTodo : TodoName) : ZIO[Database, TodoError,Unit] = {
    ZIO.accessM[Database](_.database.updateTodo(id,newTodo))
  }

  def deleteTodoById(id : Int) : ZIO[Database,TodoError,Unit] = {
    ZIO.accessM[Database](_.database.deleteTodo(id))
  }

  def getAllTodo : ZIO[Database,TodoError, List[TodoItem]] = {
    ZIO.accessM[Database](_.database.getAll)
  }

}
