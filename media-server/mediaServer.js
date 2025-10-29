const express = require('express');
const https = require('https');
const fs = require('fs');
const mediasoup = require('mediasoup');
const cors = require('cors');
const { Server } = require('socket.io');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;

// Mediasoup workers
let workers = [];
let nextWorkerIdx = 0;

// Rooms storage: roomId -> Room object
const rooms = new Map();

// Mediasoup configuration
const mediaCodecs = [
  {
    kind: 'audio',
    mimeType: 'audio/opus',
    clockRate: 48000,
    channels: 2
  },
  {
    kind: 'video',
    mimeType: 'video/VP8',
    clockRate: 90000,
    parameters: {
      'x-google-start-bitrate': 1000
    }
  },
  {
    kind: 'video',
    mimeType: 'video/H264',
    clockRate: 90000,
    parameters: {
      'packetization-mode': 1,
      'profile-level-id': '42e01f',
      'level-asymmetry-allowed': 1
    }
  }
];

// Room class to manage each session
class Room {
  constructor(roomId, worker) {
    this.roomId = roomId;
    this.worker = worker;
    this.router = null;
    this.peers = new Map(); // peerId -> Peer object
    this.isRecording = false;
    this.recordingProcess = null;
  }

  async init() {
    this.router = await this.worker.createRouter({ mediaCodecs });
    console.log(`Router created for room ${this.roomId}`);
  }

  addPeer(peerId, socket) {
    const peer = new Peer(peerId, socket, this);
    this.peers.set(peerId, peer);
    return peer;
  }

  removePeer(peerId) {
    const peer = this.peers.get(peerId);
    if (peer) {
      peer.close();
      this.peers.delete(peerId);
    }
  }

  getPeer(peerId) {
    return this.peers.get(peerId);
  }

  broadcast(senderId, event, data) {
    this.peers.forEach((peer, peerId) => {
      if (peerId !== senderId) {
        peer.socket.emit(event, data);
      }
    });
  }
}

// Peer class to represent each participant
class Peer {
  constructor(peerId, socket, room) {
    this.peerId = peerId;
    this.socket = socket;
    this.room = room;
    this.transports = new Map(); // transportId -> Transport
    this.producers = new Map(); // producerId -> Producer
    this.consumers = new Map(); // consumerId -> Consumer
  }

  async createWebRtcTransport(direction) {
  const {
    maxIncomingBitrate,
    initialAvailableOutgoingBitrate
  } = {
    maxIncomingBitrate: 1500000,
    initialAvailableOutgoingBitrate: 1000000
  };

  const transport = await this.room.router.createWebRtcTransport({
    listenIps: [
      {
        ip: '0.0.0.0',
        announcedIp: process.env.ANNOUNCED_IP || '127.0.0.1'
      }
    ],
    enableUdp: true,
    enableTcp: true,
    preferUdp: true,
    initialAvailableOutgoingBitrate,
    appData: { 
      consuming: direction === 'recv',  // ✅ ADD THIS
      producing: direction === 'send'    // ✅ ADD THIS
    }
  });

  if (maxIncomingBitrate) {
    try {
      await transport.setMaxIncomingBitrate(maxIncomingBitrate);
    } catch (error) {
      console.error('Error setting max incoming bitrate:', error);
    }
  }

  this.transports.set(transport.id, transport);

  return {
    id: transport.id,
    iceParameters: transport.iceParameters,
    iceCandidates: transport.iceCandidates,
    dtlsParameters: transport.dtlsParameters
  };
}

  async connectTransport(transportId, dtlsParameters) {
    const transport = this.transports.get(transportId);
    if (!transport) {
      throw new Error('Transport not found');
    }
    await transport.connect({ dtlsParameters });
  }

  async produce(transportId, kind, rtpParameters) {
    const transport = this.transports.get(transportId);
    if (!transport) {
      throw new Error('Transport not found');
    }

    const producer = await transport.produce({ kind, rtpParameters });
    this.producers.set(producer.id, producer);

    producer.on('transportclose', () => {
      console.log(`Producer ${producer.id} transport closed`);
      this.producers.delete(producer.id);
    });

    // Notify other peers about new producer
    this.room.broadcast(this.peerId, 'newProducer', {
      peerId: this.peerId,
      producerId: producer.id,
      kind: producer.kind
    });

    return producer.id;
  }

  async consume(producerId, rtpCapabilities) {
    const producer = this.getProducerById(producerId);
    if (!producer) {
      throw new Error('Producer not found');
    }

    if (!this.room.router.canConsume({ producerId, rtpCapabilities })) {
      throw new Error('Cannot consume');
    }

    // Get the transport for consuming
    const transport = Array.from(this.transports.values()).find(t => t.appData.consuming);
    if (!transport) {
      throw new Error('No consuming transport available');
    }

    const consumer = await transport.consume({
      producerId,
      rtpCapabilities,
      paused: true
    });

    this.consumers.set(consumer.id, consumer);

    consumer.on('transportclose', () => {
      console.log(`Consumer ${consumer.id} transport closed`);
      this.consumers.delete(consumer.id);
    });

    consumer.on('producerclose', () => {
      console.log(`Consumer ${consumer.id} producer closed`);
      this.socket.emit('consumerClosed', { consumerId: consumer.id });
      this.consumers.delete(consumer.id);
    });

    return {
      id: consumer.id,
      producerId,
      kind: consumer.kind,
      rtpParameters: consumer.rtpParameters,
      type: consumer.type,
      producerPaused: consumer.producerPaused
    };
  }

  getProducerById(producerId) {
    // Check own producers
    if (this.producers.has(producerId)) {
      return this.producers.get(producerId);
    }

    // Check other peers' producers
    for (const peer of this.room.peers.values()) {
      if (peer.producers.has(producerId)) {
        return peer.producers.get(producerId);
      }
    }

    return null;
  }

  close() {
    this.transports.forEach(transport => transport.close());
    this.producers.forEach(producer => producer.close());
    this.consumers.forEach(consumer => consumer.close());
    
    this.transports.clear();
    this.producers.clear();
    this.consumers.clear();
  }
}

// Initialize Mediasoup workers
async function initializeWorkers() {
  const numWorkers = require('os').cpus().length;
  console.log(`Creating ${numWorkers} mediasoup workers...`);

  for (let i = 0; i < numWorkers; i++) {
    const worker = await mediasoup.createWorker({
      logLevel: 'warn',
      rtcMinPort: 10000,
      rtcMaxPort: 10100
    });

    worker.on('died', () => {
      console.error(`Mediasoup worker ${i} died, exiting...`);
      process.exit(1);
    });

    workers.push(worker);
  }

  console.log('Mediasoup workers created');
}

// Get next worker in round-robin fashion
function getNextWorker() {
  const worker = workers[nextWorkerIdx];
  nextWorkerIdx = (nextWorkerIdx + 1) % workers.length;
  return worker;
}

// Get or create room
async function getOrCreateRoom(roomId) {
  if (!rooms.has(roomId)) {
    const worker = getNextWorker();
    const room = new Room(roomId, worker);
    await room.init();
    rooms.set(roomId, room);
    console.log(`Room ${roomId} created`);
  }
  return rooms.get(roomId);
}

// REST API Endpoints

// Get router RTP capabilities
app.get('/api/room/:roomId/rtp-capabilities', async (req, res) => {
  try {
    const { roomId } = req.params;
    const room = await getOrCreateRoom(roomId);
    
    res.json({ rtpCapabilities: room.router.rtpCapabilities });
  } catch (error) {
    console.error('Error getting RTP capabilities:', error);
    res.status(500).json({ error: error.message });
  }
});

// Create WebRTC transport
app.post('/api/room/:roomId/create-transport', async (req, res) => {
  try {
    const { roomId } = req.params;
    const { peerId, direction } = req.body;
    
    const room = await getOrCreateRoom(roomId);
    const peer = room.getPeer(peerId);
    
    if (!peer) {
      return res.status(404).json({ error: 'Peer not found' });
    }
    
    const transportParams = await peer.createWebRtcTransport(direction);
    res.json(transportParams);
  } catch (error) {
    console.error('Error creating transport:', error);
    res.status(500).json({ error: error.message });
  }
});

// Connect transport
app.post('/api/room/:roomId/connect-transport', async (req, res) => {
  try {
    const { roomId } = req.params;
    const { peerId, transportId, dtlsParameters } = req.body;
    
    const room = rooms.get(roomId);
    if (!room) {
      return res.status(404).json({ error: 'Room not found' });
    }
    
    const peer = room.getPeer(peerId);
    if (!peer) {
      return res.status(404).json({ error: 'Peer not found' });
    }
    
    await peer.connectTransport(transportId, dtlsParameters);
    res.json({ success: true });
  } catch (error) {
    console.error('Error connecting transport:', error);
    res.status(500).json({ error: error.message });
  }
});

// Produce media
app.post('/api/room/:roomId/produce', async (req, res) => {
  try {
    const { roomId } = req.params;
    const { peerId, transportId, kind, rtpParameters } = req.body;
    
    const room = rooms.get(roomId);
    if (!room) {
      return res.status(404).json({ error: 'Room not found' });
    }
    
    const peer = room.getPeer(peerId);
    if (!peer) {
      return res.status(404).json({ error: 'Peer not found' });
    }
    
    const producerId = await peer.produce(transportId, kind, rtpParameters);
    res.json({ producerId });
  } catch (error) {
    console.error('Error producing:', error);
    res.status(500).json({ error: error.message });
  }
});

// Consume media
app.post('/api/room/:roomId/consume', async (req, res) => {
  try {
    const { roomId } = req.params;
    const { peerId, producerId, rtpCapabilities } = req.body;
    
    const room = rooms.get(roomId);
    if (!room) {
      return res.status(404).json({ error: 'Room not found' });
    }
    
    const peer = room.getPeer(peerId);
    if (!peer) {
      return res.status(404).json({ error: 'Peer not found' });
    }
    
    const consumerParams = await peer.consume(producerId, rtpCapabilities);
    res.json(consumerParams);
  } catch (error) {
    console.error('Error consuming:', error);
    res.status(500).json({ error: error.message });
  }
});

// Resume consumer
app.post('/api/room/:roomId/resume-consumer', async (req, res) => {
  try {
    const { roomId } = req.params;
    const { peerId, consumerId } = req.body;
    
    const room = rooms.get(roomId);
    if (!room) {
      return res.status(404).json({ error: 'Room not found' });
    }
    
    const peer = room.getPeer(peerId);
    if (!peer) {
      return res.status(404).json({ error: 'Peer not found' });
    }
    
    const consumer = peer.consumers.get(consumerId);
    if (!consumer) {
      return res.status(404).json({ error: 'Consumer not found' });
    }
    
    await consumer.resume();
    res.json({ success: true });
  } catch (error) {
    console.error('Error resuming consumer:', error);
    res.status(500).json({ error: error.message });
  }
});

// Start recording
app.post('/api/recording/start', async (req, res) => {
  try {
    const { roomId, recordingId } = req.body;
    
    const room = rooms.get(roomId);
    if (!room) {
      return res.status(404).json({ error: 'Room not found' });
    }
    
    if (room.isRecording) {
      return res.status(400).json({ error: 'Already recording' });
    }
    
    // Start recording logic here (using FFmpeg or GStreamer)
    room.isRecording = true;
    
    console.log(`Started recording for room ${roomId}, recordingId: ${recordingId}`);
    res.json({ success: true, recordingId });
  } catch (error) {
    console.error('Error starting recording:', error);
    res.status(500).json({ error: error.message });
  }
});

// Stop recording
app.post('/api/recording/stop', async (req, res) => {
  try {
    const { roomId, recordingId } = req.body;
    
    const room = rooms.get(roomId);
    if (!room) {
      return res.status(404).json({ error: 'Room not found' });
    }
    
    if (!room.isRecording) {
      return res.status(400).json({ error: 'Not recording' });
    }
    
    // Stop recording logic here
    room.isRecording = false;
    
    // Notify Spring Boot backend about recording completion
    // You would implement this to call the webhook endpoint
    
    console.log(`Stopped recording for room ${roomId}, recordingId: ${recordingId}`);
    res.json({ success: true, recordingId });
  } catch (error) {
    console.error('Error stopping recording:', error);
    res.status(500).json({ error: error.message });
  }
});

// Socket.IO for signaling
const server = app.listen(PORT, () => {
  console.log(`Media server running on port ${PORT}`);
});

const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
});

io.on('connection', (socket) => {
  console.log('Client connected:', socket.id);
  
  socket.on('join-room', async ({ roomId, peerId }) => {
    try {
      const room = await getOrCreateRoom(roomId);
      const peer = room.addPeer(peerId, socket);
      
      socket.join(roomId);
      socket.roomId = roomId;
      socket.peerId = peerId;
      
      // Send existing producers to new peer
      const producers = [];
      room.peers.forEach((p, pid) => {
        if (pid !== peerId) {
          p.producers.forEach((producer) => {
            producers.push({
              peerId: pid,
              producerId: producer.id,
              kind: producer.kind
            });
          });
        }
      });
      
      socket.emit('joined-room', { producers });
      
      // Notify other peers
      socket.to(roomId).emit('peer-joined', { peerId });
      
      console.log(`Peer ${peerId} joined room ${roomId}`);
    } catch (error) {
      console.error('Error joining room:', error);
      socket.emit('error', { message: error.message });
    }
  });
  
  socket.on('leave-room', () => {
    if (socket.roomId && socket.peerId) {
      const room = rooms.get(socket.roomId);
      if (room) {
        room.removePeer(socket.peerId);
        socket.to(socket.roomId).emit('peer-left', { peerId: socket.peerId });
        
        // Clean up empty rooms
        if (room.peers.size === 0) {
          room.router.close();
          rooms.delete(socket.roomId);
          console.log(`Room ${socket.roomId} closed`);
        }
      }
      
      socket.leave(socket.roomId);
      console.log(`Peer ${socket.peerId} left room ${socket.roomId}`);
    }
  });
  
  socket.on('disconnect', () => {
    if (socket.roomId && socket.peerId) {
      const room = rooms.get(socket.roomId);
      if (room) {
        room.removePeer(socket.peerId);
        socket.to(socket.roomId).emit('peer-left', { peerId: socket.peerId });
        
        if (room.peers.size === 0) {
          room.router.close();
          rooms.delete(socket.roomId);
          console.log(`Room ${socket.roomId} closed`);
        }
      }
    }
    console.log('Client disconnected:', socket.id);
  });
});

// Initialize and start server
(async () => {
  await initializeWorkers();
  console.log('Media server initialized successfully');
})();