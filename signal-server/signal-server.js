"use strict";

const http = require('http');
const WebSocketServer = require('websocket').server;

var connections = [];

const setUserReady = (socket, userId) => {
  if (socket.session.traineeId === userId) {
    socket.traineeReady = true;
    sendToTarget(socket.session.instructorId, {type: "target_is_ready"});
  } else if (socket.session.instructorId === userId) {
   socket.instructorReady = true;
   sendToTarget(socket.session.traineeId, {type: "target_is_ready"});
  }

  if (socket.traineeReady && socket.instructorReady) {
    sendToTarget(socket.session.instructorId, {type: "start_session"});
  }
}

const log = text => {
  const time = new Date();
  console.log("[" + time.toLocaleTimeString() + "] " + text);
}

const sendToTarget = (target, message) => {
  const payload = JSON.stringify(message);
  connections
    .filter(connection => connection.user.credentials.id === target)
    .forEach(connection => connection.sendUTF(payload));
}

const httpServer = http.createServer((request, response) => {
  log("Received request for " + request.url);
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

  socket.on("message", data => {
    if (data.type !== 'utf8') {
      return;
    }
    const message = JSON.parse(data.utf8Data);       
    switch (message.type) {
      case "initiate":
        socket.session = message.payload.session;
        socket.user = message.payload.user;
        break;
      case "user_ready":
        setUserReady(socket, message.payload);
        break;
      case "message":
        message.text = message.text.replace(/(<([^>]+)>)/ig, "");
        sendToTarget(message.target, message);
        break;
      default:
        log(`Received ${message.type} from ${message.sender} to ${message.target}.`);
        sendToTarget(message.target, message);
    }
  });

  socket.on("close", (reason, description) => {
    connections = connections.filter(connection => connection.connected);
    log(`Connection lost from ${socket.remoteAddress}: ${reason}. \n\t${description}`);
  });
});
