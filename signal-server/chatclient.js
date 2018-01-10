"use strict";

document.getElementById("instructor").addEventListener("click", event => {
  apiResponse = instructorResponse;
  connect();
});

document.getElementById("trainee").addEventListener("click", event => {
  apiResponse = traineeResponse;
  connect();
});

document.getElementById("start").addEventListener("click", event => {
  invite(event);
});


const instructorResponse = {
  userId: 15,
  userName: "Kaplan",
  userType: "instructor",
  targetId: 4,
  targetName: "Bora"
};

const traineeResponse = {
  userId: 4,
  userName: "Bora",
  userType: "trainee",
  targetId: 15,
  targetName: "Kaplan"
};

// Example result from server if user is trainee
var apiResponse;

const mediaConstraints = {
  audio: true,
  video: true
};

var socket = null;
var peerConnection = null;
var hasAddTrack = false;


function log(text) {
  let time = new Date();
  console.log("[" + time.toLocaleTimeString() + "] " + text);
}

function log_error(text) {
  let time = new Date();
  console.error("[" + time.toLocaleTimeString() + "] " + text);
}

function sendToServer(message) {
  let msgJSON = JSON.stringify(message);
  log("Sending '" + message.type + "' message: " + msgJSON);
  socket.send(msgJSON);
}

function connect() {
  socket = new WebSocket("ws://192.168.1.39:6503", "json");
  socket.onopen = (event) => {
    sendToServer({
      type: "id",
      id: apiResponse.userId
    });
    document.getElementById("text").disabled = false;
    document.getElementById("send").disabled = false;
  };

  socket.onmessage = (event) => {
    let message = JSON.parse(event.data);
    console.log("MESSAGE: ");
    console.dir(message);
    let time = new Date(message.date);
    let timeStr = time.toLocaleTimeString();

    switch (message.type) {
      case "message":
        let chatFrameDocument = document.getElementById("chatbox").contentDocument;
        let text = "(" + timeStr + ") <b>" + message.name + "</b>: " + message.text + "<br>";
        chatFrameDocument.write(text);
        document.getElementById("chatbox").contentWindow.scrollByPages(1);
        break;

      case "video-offer":  // Invitation and offer to chat
        handleVideoOfferMsg(message);
        break;

      case "video-answer":  // Callee has answered our offer
        handleVideoAnswerMsg(message);
        break;

      case "new-ice-candidate": // A new ICE candidate has been received
        handleNewICECandidateMsg(message);
        break;

      case "hang-up": // The other peer has hung up the call
        handleHangUpMsg(message);
        break;

      default:
        log_error("Unknown message received:");
        log_error(message);
    }
  };
}

// Handles a click on the Send button (or pressing return/enter) by
// building a "message" object and sending it to the server.
function handleSendButton() {
  var msg = {
    text: document.getElementById("text").value,
    type: "message",
    id: apiResponse.userId,
    date: Date.now()
  };
  sendToServer(msg);
  document.getElementById("text").value = "";
}

// Handler for keyboard events. This is used to intercept the return and
// enter keys so that we can call send() to transmit the entered text
// to the server.
function handleKey(event) {
  if (event.keyCode === 13 || event.keyCode === 14) {
    if (!document.getElementById("send").disabled) {
      handleSendButton();
    }
  }
}

function createPeerConnection() {
  log("Setting up a socket...");
  peerConnection = new RTCPeerConnection({
    iceServers: [
      {
        urls: "turn:numb.viagenie.ca:3478",
        username: "procsyma",
        credential: "159896"
      }
    ]
  });

  peerConnection.onicecandidate = handleICECandidateEvent;
  peerConnection.onnremovestream = handleRemoveStreamEvent;
  peerConnection.oniceconnectionstatechange = handleICEConnectionStateChangeEvent;
  peerConnection.onicegatheringstatechange = handleICEGatheringStateChangeEvent;
  peerConnection.onsignalingstatechange = handleSignalingStateChangeEvent;
  peerConnection.onnegotiationneeded = handleNegotiationNeededEvent;

  hasAddTrack = (peerConnection.addTrack !== undefined);

  if (hasAddTrack) {
    peerConnection.ontrack = handleTrackEvent;
  } else {
    peerConnection.onaddstream = handleAddStreamEvent;
  }
}

function handleNegotiationNeededEvent() {
  log("*** Negotiation needed");
  log("---> Creating offer");
  peerConnection.createOffer()
    .then(offer => {
      log("---> Creating new description object to send to remote peer");
      return peerConnection.setLocalDescription(offer);
    })
    .then(() => {
      log("---> Sending offer to remote peer");
      sendToServer({
        sender: apiResponse.userId,
        target: apiResponse.targetId,
        type: "video-offer",
        sdp: peerConnection.localDescription
      });
    })
    .catch(reportError);
}

function handleTrackEvent(event) {
  log("*** Track event");
  document.getElementById("received_video").srcObject = event.streams[0];
  document.getElementById("hangup-button").disabled = false;
}

function handleAddStreamEvent(event) {
  log("*** Stream added");
  document.getElementById("received_video").srcObject = event.stream;
  document.getElementById("hangup-button").disabled = false;
}

function handleRemoveStreamEvent(event) {
  log("*** Stream removed");
  closeVideoCall();
}

function handleICECandidateEvent(event) {
  if (event.candidate) {
    log("Outgoing ICE candidate: " + event.candidate.candidate);
    sendToServer({
      type: "new-ice-candidate",
      target: apiResponse.targetId,
      candidate: event.candidate
    });
  }
}

function handleICEConnectionStateChangeEvent(event) {
  log("*** ICE socket state changed to " + peerConnection.iceConnectionState);
  switch (peerConnection.iceConnectionState) {
    case "closed":
    case "failed":
    case "disconnected":
      closeVideoCall();
      break;
  }
}

function handleSignalingStateChangeEvent(event) {
  log("*** WebRTC signaling state changed to: " + peerConnection.signalingState);
  switch (peerConnection.signalingState) {
    case "closed":
      closeVideoCall();
      break;
  }
}

function handleICEGatheringStateChangeEvent(event) {
  log("*** ICE gathering state changed to: " + peerConnection.iceGatheringState);
  // if (peerConnection.iceGatheringState === "complete") {
  // }
}

function closeVideoCall() {
  let remoteVideo = document.getElementById("received_video");
  let localVideo = document.getElementById("local_video");

  log("Closing the call");

  if (peerConnection) {
    log("--> Closing the peer socket");
    peerConnection.onaddstream = null;
    peerConnection.ontrack = null;
    peerConnection.onremovestream = null;
    peerConnection.onnicecandidate = null;
    peerConnection.oniceconnectionstatechange = null;
    peerConnection.onsignalingstatechange = null;
    peerConnection.onicegatheringstatechange = null;
    peerConnection.onnotificationneeded = null;

    if (remoteVideo.srcObject) {
      remoteVideo.srcObject.getTracks().forEach(track => track.stop());
    }

    if (localVideo.srcObject) {
      localVideo.srcObject.getTracks().forEach(track => track.stop());
    }

    remoteVideo.src = null;
    localVideo.src = null;
    peerConnection.close();
    peerConnection = null;
  }

  document.getElementById("hangup-button").disabled = true;
}

function handleHangUpMsg(message) {
  log("*** Received hang up notification from other peer");
  closeVideoCall();
}

function hangUpCall() {
  closeVideoCall();
  sendToServer({
    sender: apiResponse.userId,
    target: apiResponse.targetId,
    type: "hang-up"
  });
}

function invite(event) {
  log("Starting to prepare an invitation");
  if (peerConnection) {
    alert("You can't start a call because you already have one open!");
  }

  let targetId = apiResponse.targetId;
  log("Inviting user " + targetId);
  log("Setting up socket to invite user: " + targetId);
  createPeerConnection();

  log("Requesting webcam access...");
  navigator.mediaDevices.getUserMedia(mediaConstraints)
    .then(localStream => {
      log("-- Local video stream obtained");
      document.getElementById("local_video").src = window.URL.createObjectURL(localStream);
      document.getElementById("local_video").srcObject = localStream;

      if (hasAddTrack) {
        log("-- Adding tracks to the RTCPeerConnection");
        localStream.getTracks().forEach(track => peerConnection.addTrack(track, localStream));
      } else {
        log("-- Adding stream to the RTCPeerConnection");
        peerConnection.addStream(localStream);
      }
    })
    .catch(handleGetUserMediaError);
}

function handleVideoOfferMsg(message) {
  let localStream = null;
  const targetId = apiResponse.targetId;
  log("Starting to accept invitation from " + targetId);
  createPeerConnection();
  const desc = new RTCSessionDescription(message.sdp);

  peerConnection.setRemoteDescription(desc)
    .then(() => {
      log("Setting up the local media stream...");
      return navigator.mediaDevices.getUserMedia(mediaConstraints);
    })
    .then(stream => {
      log("-- Local video stream obtained");
      localStream = stream;
      document.getElementById("local_video").src = window.URL.createObjectURL(localStream);
      document.getElementById("local_video").srcObject = localStream;

      if (hasAddTrack) {
        log("-- Adding tracks to the RTCPeerConnection");
        localStream.getTracks().forEach(track => peerConnection.addTrack(track, localStream));
      } else {
        log("-- Adding stream to the RTCPeerConnection");
        peerConnection.addStream(localStream);
      }

    })
    .then(() => {
      log("------> Creating answer");
      return peerConnection.createAnswer();
    })
    .then(answer => {
      log("------> Setting local description after creating answer");
      return peerConnection.setLocalDescription(answer);
    })
    .then(() => {
      log("Sending answer packet back to other peer");
      sendToServer({
        sender: apiResponse.userId,
        target: targetId,
        type: "video-answer",
        sdp: peerConnection.localDescription
      });
    })
    .catch(handleGetUserMediaError);
}

function handleVideoAnswerMsg(message) {
  log("Call recipient has accepted our call");
  const desc = new RTCSessionDescription(message.sdp);
  peerConnection.setRemoteDescription(desc).catch(reportError);
}

function handleNewICECandidateMsg(message) {
  const candidate = new RTCIceCandidate(message.candidate);
  log("Adding received ICE candidate: " + JSON.stringify(candidate));
  peerConnection.addIceCandidate(candidate)
    .catch(reportError);
}

function handleGetUserMediaError(error) {
  log(error);
  switch (error.name) {
    case "NotFoundError":
      alert("Unable to open your call because no camera and/or microphone were found.");
      break;
    case "SecurityError":
    case "PermissionDeniedError":
      break;
    default:
      alert("Error opening your camera and/or microphone: " + error.message);
      break;
  }
  closeVideoCall();
}

function reportError(error) {
  log_error("Error " + error.name + ": " + error.message);
}
