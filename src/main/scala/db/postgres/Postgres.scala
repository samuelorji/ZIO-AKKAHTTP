package db.postgres

import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ConnectionPool, PoolConfiguration}
import com.github.mauricio.async.db.{Configuration, QueryResult, ResultSet}
import config.AppConfig
import db.Database
import domain._
import zio.{IO, Task, UIO, ZIO}

import scala.util.Try

sealed trait DatabaseDriver {
  private val configuration = new Configuration(
    username = AppConfig.postgresDbUser,
    host = AppConfig.postgresDbHost,
    port = AppConfig.postgresDbPort,
    password = Some(AppConfig.postgresDbPass),
    database = Some(AppConfig.postgresDbName)

  )

  private val poolConfiguration = new PoolConfiguration(
    maxObjects = AppConfig.postgresDbPoolMaxObjects,
    maxIdle = AppConfig.postgresDbPoolMaxIdle,
    maxQueueSize = AppConfig.postgresDbPoolMaxQueueSize
  )

  private val factory = new MySQLConnectionFactory(configuration)


  private lazy val pool = new ConnectionPool(factory, poolConfiguration)
  lazy val db: UIO[ConnectionPool[MySQLConnection]] = UIO.fromFunction(_ => pool)
 }

trait PostgresDatabase extends Database with DatabaseDriver { self =>

  private def runQuery(query :String) : ZIO[DatabaseDriver,Throwable,QueryResult] = {
    for {
       db <- ZIO.accessM[DatabaseDriver](_.db)
      result <- ZIO.fromFuture(_ => db.sendPreparedStatement(query))
    }yield result
  }

  override def database: Database.Service = new Database.Service {
    override def create(todo: TodoName): IO[TodoError, Unit] = {
      val query = s"""INSERT INTO todo(name) VALUE("${todo.name}");"""
      (for {
        res<- runQuery(query).provide(self).map(_ => {})
      } yield res).mapError{mapThrowableToTodoError}
    }

    override def findTodo(id: Int): IO[TodoError, TodoItem] = {
      val query = s"select * from todo where id=$id"
      (for {
        dbResult <- runQuery(query).provide(self)
                   todo = mapTo[TodoItem](dbResult, resultSetToTodo)
        effect <- ZIO.fromOption(todo).mapError(_ =>  ToDoItemError(s"id $id doesn't exist"))
      } yield effect).mapError {  mapThrowableToTodoError }
    }

    override def updateTodo(id: Int, todo: TodoName): IO[TodoError, Unit] = {
      val query = s"""update todo set name="${todo.name}" where id=$id;"""
      (for {
        dbResult <- runQuery(query).provide(self)
        _ <- IO(println(dbResult.rowsAffected))
        res <- dbResult.rowsAffected match {
          case 1 => IO.succeed({})
          case _ => IO.fail(ToDoItemError(s"id $id doesn't exist"))
        }
      } yield res).mapError { mapThrowableToTodoError }


    }

    override def deleteTodo(id: Int): IO[TodoError, Unit] = {

      val query = s"delete from todo where id=$id;"

      (for {
        dbResult <- runQuery(query).provide(self)
        _ <- IO(println(dbResult.rowsAffected))
        res <- dbResult.rowsAffected match {
          case 1 => IO.succeed({})
          case _ => IO.fail(ToDoItemError(s"id $id doesn't exist"))
        }
      } yield res).mapError { mapThrowableToTodoError}
    }

    override def getAll: IO[TodoError, List[TodoItem]] = {
      val query = "select * from todo;"
      (for{
        dbResult <- runQuery(query).provide(self)
        res <- ZIO.fromOption(mapTo[List[TodoItem]](dbResult,resultSetToListTodo)).mapError(_ => ToDoItemError("No todo's to fetch"))
      }yield res).mapError { mapThrowableToTodoError }
    }
  }

  private def mapThrowableToTodoError(err : Throwable) : TodoError = {
    val errMsg = err.getMessage
    err match {
      case err : ToDoItemError => err
      case e  =>
        errMsg.toLowerCase.startsWith("error") match {
          case true =>
            QueryError(errMsg)
          case false =>
            DbError(e.getMessage)

        }
    }
  }

  private def mapTo[T](result : QueryResult ,f : ResultSet => Option[T]) : Option[T] = {
    result.rows match {
      case None =>
        None
      case Some(resultSet) =>
        f(resultSet)
    }
  }


  def resultSetToTodo(resultSet: ResultSet): Option[TodoItem] = {
    Try {
      resultSet.headOption.map { row =>
        val id = row("id").asInstanceOf[Int]
        val name = row("name").asInstanceOf[String]
        TodoItem(id, name)
      }
    }.getOrElse(None)
  }
  def resultSetToListTodo(resultSet: ResultSet) : Option[List[TodoItem]] = {
    Try {
      Some {
        resultSet.map { row =>
          val id = row("id").asInstanceOf[Int]
          val name = row("name").asInstanceOf[String]
          TodoItem(id, name)
        }.toList
      }
    }.getOrElse(None)
  }

  Task
}

object PostgresDB extends PostgresDatabase
