"use strict";

const http = require('http');
const WebSocketServer = require('websocket').server;

var connections = [];

function log(text) {
  const time = new Date();
  console.log("[" + time.toLocaleTimeString() + "] " + text);
}

function sendToTarget(target, message) {
  connections
    .filter(connection => connection.userId === target)
    .forEach(connection => connection.sendUTF(message));
}

const httpServer = http.createServer((request, response) => {
  log("Received secure request for " + request.url);
  response.writeHead(404);
  response.end();
});

httpServer.listen(6503, () => log("Server is listening on port 6503"));

const wsServer = new WebSocketServer({
  httpServer: httpServer,
  autoAcceptConnections: false
});

wsServer.on("request", (request) => {
  const socket = request.accept("json", request.origin);
  connections.push(socket);
  log(`Connection accepted from ${socket.remoteAddress}.`);

  socket.on("message", (msg) => {
    if (msg.type === 'utf8') {
      let message = JSON.parse(msg.utf8Data);
      switch (message.type) {
        case "id":
          socket.userId = message.id;
          break;
        case "message":
          message.text = message.text.replace(/(<([^>]+)>)/ig, "");
        default:
          log(`Received ${message.type} from ${message.sender} to ${message.target}.`);
          sendToTarget(message.target, JSON.stringify(message));
      }
    }
  });

  socket.on("close", (reason, description) => {
    // connections = connections.filter((el, idx, ar) => el.connected);
    connections = connections.filter(connection => connection.connected);
    log(`Connection lost from ${socket.remoteAddress}: ${reason}. \n\t${description}`);
  });
});
