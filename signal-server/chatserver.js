"use strict";

var http = require('http');
var WebSocketServer = require('websocket').server;

var connectionArray = [];
var appendToMakeUnique = 1;

function log(text) {
  var time = new Date();
  console.log("[" + time.toLocaleTimeString() + "] " + text);
}

function originIsAllowed(origin) {
  return true;
}

function sendToOneUser(target, message) {
  connectionArray
    .filter(connection => connection.userId === target)
    .forEach(connection => connection.sendUTF(message));
}

function getConnectionForID(id) {
  return connectionArray.find(connection => connection.userId = id);
}

// Our HTTPS server does nothing but service WebSocket
// connections, so every request just returns 404. Real Web
// requests are handled by the main server on the box. If you
// want to, you can return real HTML here and serve Web content.

const httpServer = http.createServer((request, response) => {
  log("Received secure request for " + request.url);
  response.writeHead(404);
  response.end();
});

httpServer.listen(6503, () => log("Server is listening on port 6503"));

// Create the WebSocket server by converting the HTTPS server into one.

const wsServer = new WebSocketServer({
  httpServer: httpServer,
  autoAcceptConnections: false
});

// Set up a "connect" message handler on our WebSocket server. This is
// called whenever a user connects to the server's port using the
// WebSocket protocol.

wsServer.on("request", (request) => {
  if (!originIsAllowed(request.origin)) {
    request.reject();
    log("Connection from " + request.origin + " rejected.");
    return;
  }

  const connection = request.accept("json", request.origin);
  log("Connection accepted from " + connection.remoteAddress + ".");
  connectionArray.push(connection);

  // Set up a handler for the "message" event received over WebSocket. This
  // is a message sent by a client, and may be text to share with other
  // users, a private message (text or signaling) for one user, or a command
  // to the server.

  connection.on("message", (message) => {
    if (message.type === 'utf8') {

      let msg = JSON.parse(message.utf8Data);
      log("Received Message: " + msg.type);

      // if (!connection.userId) {
      //   connection.userId = msg.sender
      // }
      //
      // let connect = getConnectionForID(msg.sender);

      switch (msg.type) {
        case "id":
          connection.userId = msg.id;
          break;
        // Public, textual message
        case "message":
          msg.name = connection.username;
          msg.text = msg.text.replace(/(<([^>]+)>)/ig, "");
          console.log("Sending message from " + msg.sender + " to " + msg.target);
          sendToOneUser(msg.target, JSON.stringify(msg));
          break;
        default:
          console.log("Sending message from " + msg.sender + " to " + msg.target);
          sendToOneUser(msg.target, JSON.stringify(msg));
          break;
      }
    }
  });

  // Handle the WebSocket "close" event; this means a user has logged off
  // or has been disconnected.
  connection.on('close', (reason, description) => {
    // First, remove the socket from the list of connections.
    connectionArray = connectionArray.filter((el, idx, ar) => {
      return el.connected;
    });

    // Now send the updated user list. Again, please don't do this in a
    // real application. Your users won't like you very much.

    // Build and output log output for close information.

    let logMessage = "Connection closed: " + connection.remoteAddress + " (" +
      reason;
    if (description !== null && description.length !== 0) {
      logMessage += ": " + description;
    }
    logMessage += ")";
    log(logMessage);
  });
});
