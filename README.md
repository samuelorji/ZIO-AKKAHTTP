# ZIO-AKKAHTTP

*Codebase for this article on beginning functional programming using ZIO* 

A really Simple Todo App built functionally using ZIO and Akka HTTP to connect to a Mysql Database.
The codebase is quite verbose and riddled with comments to help the newbie understand the thought process

## Running the project
**all commands are run from the root of the project**
## Starting the Database
`docker-compose -f ./scripts/docker/docker-compose.yml up` 

Once the database is up and running, run this script to create the database and tables as well as insert mock data. 

`./scripts/deploy.sh`

## Starting the server 

to specify port to run on, you can pass the port argument to sbt 

`sbt "run <port_number>"`

## API ROUTES
`GET -> /todo`      

Get All Todos


`POST -> /todo`     <<  {"name" : "todo_name"}

Create new Todo


`GET,DELETE -> /todo/1`     

Get or delete Todo with Id 1


