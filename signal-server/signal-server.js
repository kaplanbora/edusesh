"use strict";

const http = require('http');
const WebSocketServer = require('websocket').server;

var connections = [];
var sessions = [];

const setUserReady = (socket, userId) => {
  const session = sessions.filter(s => s.id == socket.session)[0];
  if (socket.role === "trainee") {
    session.traineeReady = true;
    sendToTarget(socket.target, {type: "target_is_ready"}, socket.owner);
  } else {
    session.instructorReady = true;
    sendToTarget(socket.target, {type: "target_is_ready"}, socket.owner);
  }

  setTimeout(() => {
    if (session.traineeReady && session.instructorReady) {
      session.startDate = new Date();
      const message = {
        type: "start_session",
        payload: session.startDate
      };
      sendToTarget(socket.owner, message, socket.owner);
      sendToTarget(socket.target, message, socket.owner);
    }
  }, 800);
}

const log = text => {
  const time = new Date();
  console.log("[" + time.toLocaleTimeString() + "] " + text);
}

const sendToTarget = (target, message, owner) => {
  const payload = JSON.stringify(message);
  console.log(`[SENDING]: Type: ${message.type} Target: ${target} Owner: ${owner}`);
  connections
    .filter(connection => connection.owner == target)
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
    //connections.forEach(socket => console.log(`[RECEIVED-BEFORE] - Type: ${message.type} Owner: ${socket.owner} Target: ${socket.target} Session: ${socket.session}`));
    sessions.forEach(s => console.log(`[SESSIONS-BEFORE] - ID:${s.id} InstructorReady:${s.instructorReady} TraineeReady: ${s.traineeReady}`));
    switch (message.type) {
      case "initiate":
        socket.owner = message.payload.owner;
        socket.target = message.payload.target;
        socket.session = message.payload.session;
        socket.role = message.payload.role;
        if (sessions.filter(session => session.id == message.payload.session).length == 0) {
          sessions.push({
            id: message.payload.session,
            instructorReady: false,
            traineeReady: false,
            startDate: null
          });
        }
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
    //connections.forEach(socket => console.log(`[RECEIVED-AFTER] - Type: ${message.type} Owner: ${socket.owner} Target: ${socket.target} Session: ${socket.session}`));
    sessions.forEach(s => console.log(`[SESSIONS-AFTER] - ID:${s.id} InstructorReady:${s.instructorReady} TraineeReady: ${s.traineeReady}`));
  });

  socket.on("close", (reason, description) => {
    connections = connections.filter(connection => connection.connected);
    log(`Connection lost from ${socket.remoteAddress}: ${reason}. \n\t${description}`);
  });
});
