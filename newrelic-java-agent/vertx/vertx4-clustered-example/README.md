# vertx4-clustered-example

This project demonstrates a simple two node verticle cluster that runs on localhost. It's made up of two small verticle
projects that can be run as two distinct processes:
- `MessageSender` - A verticle that starts up an HTTP server that can receive `POST` requests to `/send/:messager`.
It extracts the string from the `:message` path variable and delivers it to any other verticles (nodes) listening to the
`inbox` address.
- `MessageReceiver` - Simple verticle that consumes message on the `inbox` address and dumps the received message to
stdout.

Both projects contain a `Starter` class which contains the projects `main` method and is responsible configuring the
cluster and deploying the target verticle instance.

### Building the Projects
Requires a Java 17 JDK.

In the top level folder for each project (`MessageReceiver` and `MessageSender`), simple execute `./mvnw clean package`.
This will create a runnable "fat" jar in the `target` folder for each project.

### Running
For this example, you can have a single message sender app running (since right now it's hardcoded to listen on port `8080`) and
multiple message receivers. 

`MessageSender`: `java -jar target/MessageSender-1.0-SNAPSHOT.jar`
`MessageReceiver`: `java -jar target/MessageReceiver-1.0-SNAPSHOT.jar`

You can execute each in a separate shell window or run each one as a background process. Remember that you can have a single
`MessageSender` instance and multiple `MessageReceiver` instances.

Executing `curl -X POST http://localhost:8080/send/hello_world` will dump the following to stdout for each receiver:
```text
Received message: hello_world
```
