/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.emqx.exhook;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.msgpack.jackson.dataformat.MessagePackFactory;

public class ExServer {
    private static final Logger logger = Logger.getLogger(ExServer.class.getName());
    private static final ObjectMapper msgpackMapper = new ObjectMapper(new MessagePackFactory());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 9000;

        server = ServerBuilder.forPort(port)
                .addService(new HookProviderImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown
                // hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ExServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon
     * threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final ExServer server = new ExServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class HookProviderImpl extends HookProviderGrpc.HookProviderImplBase {

        public void DEBUG(String fn, Object req) {
            System.out.printf(fn + ", request: " + req);
        }

        @Override
        public void onProviderLoaded(ProviderLoadedRequest request, StreamObserver<LoadedResponse> responseObserver) {
            DEBUG("onProviderLoaded", request);
            HookSpec[] specs = {
                    HookSpec.newBuilder().setName("client.connect").build(),
                    HookSpec.newBuilder().setName("client.connack").build(),
                    HookSpec.newBuilder().setName("client.connected").build(),
                    HookSpec.newBuilder().setName("client.disconnected").build(),
                    HookSpec.newBuilder().setName("client.authenticate").build(),
                    HookSpec.newBuilder().setName("client.authorize").build(),
                    HookSpec.newBuilder().setName("client.subscribe").build(),
                    HookSpec.newBuilder().setName("client.unsubscribe").build(),

                    HookSpec.newBuilder().setName("session.created").build(),
                    HookSpec.newBuilder().setName("session.subscribed").build(),
                    HookSpec.newBuilder().setName("session.unsubscribed").build(),
                    HookSpec.newBuilder().setName("session.resumed").build(),
                    HookSpec.newBuilder().setName("session.discarded").build(),
                    HookSpec.newBuilder().setName("session.takenover").build(),
                    HookSpec.newBuilder().setName("session.terminated").build(),

                    HookSpec.newBuilder().setName("message.publish").build(),
                    HookSpec.newBuilder().setName("message.delivered").build(),
                    HookSpec.newBuilder().setName("message.acked").build(),
                    HookSpec.newBuilder().setName("message.dropped").build()
            };
            LoadedResponse reply = LoadedResponse.newBuilder().addAllHooks(Arrays.asList(specs)).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onProviderUnloaded(ProviderUnloadedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onProviderUnloaded", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientConnect(ClientConnectRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientConnect", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientConnack(ClientConnackRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientConnack", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientConnected(ClientConnectedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientConnected", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientDisconnected(ClientDisconnectedRequest request,
                StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientDisconnected", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientAuthenticate(ClientAuthenticateRequest request,
                StreamObserver<ValuedResponse> responseObserver) {
            DEBUG("onClientAuthenticate", request);
            ValuedResponse reply = ValuedResponse.newBuilder()
                    .setBoolResult(true)
                    .setType(ValuedResponse.ResponsedType.STOP_AND_RETURN)
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientAuthorize(ClientAuthorizeRequest request, StreamObserver<ValuedResponse> responseObserver) {
            DEBUG("onClientAuthorize", request);
            ValuedResponse reply = ValuedResponse.newBuilder()
                    .setBoolResult(true)
                    .setType(ValuedResponse.ResponsedType.STOP_AND_RETURN)
                    .build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientSubscribe(ClientSubscribeRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientSubscribe", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientUnsubscribe(ClientUnsubscribeRequest request,
                StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientUnsubscribe", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionCreated(SessionCreatedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionCreated", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionSubscribed(SessionSubscribedRequest request,
                StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionSubscribed", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionUnsubscribed(SessionUnsubscribedRequest request,
                StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionUnsubscribed", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionResumed(SessionResumedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionResumed", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionDiscarded(SessionDiscardedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionDdiscarded", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionTakenover(SessionTakenoverRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionTakenover", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionTerminated(SessionTerminatedRequest request,
                StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionTerminated", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onMessagePublish(MessagePublishRequest request, StreamObserver<ValuedResponse> responseObserver) {
            DEBUG("onMessagePublish", request);

            ByteString bstr = ByteString.copyFromUtf8("hardcode payload by exhook-svr-java :)");
            // ByteString bstr = request.getMessage().getPayload();
            String topic = request.getMessage().getTopic();

            if (topic.startsWith("BLE111444/")) {
                logger.info("xwk-iot-exhook Matched topic: " + topic);

                // 1. 获取原始消息
                Message message = request.getMessage();
                byte[] payload = message.getPayload().toByteArray();
                // 2. 解码 MessagePack 数据为 Map 结构
                Map<String, Object> data;
                try {
                    data = msgpackMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
                    });
                    logger.info("xwk-iot-exhook MessagePack Decoded message: " + data);
                    String ip = data.get("ip").toString();
                    String macAddress = data.get("mac").toString();
                    Object devicePacks = data.get("devices");
                    logger.info(macAddress + " " + ip + " " + devicePacks);
                } catch (Exception e) {
                    logger.warning(topic + " Failed to decode MessagePack data: " + e.getMessage());
                }

                // Object beacon = BeaconParser.parseIBeacon(payload);
                // if (beacon != null) {
                // String result = beacon.toString();
                // logger.info("Parsed beacon: " + result);
                // // Message.MessageResponse response = Message.MessageResponse.newBuilder()
                // // .setMessage(
                // //
                // request.toBuilder().setPayload(com.google.protobuf.ByteString.copyFromUtf8(result)))
                // // .build();
                // // responseObserver.onNext(response);
                // } else {
                // logger.warning("Failed to parse beacon data for topic: " + topic);
                // //
                // responseObserver.onNext(Message.MessageResponse.newBuilder().setMessage(request).build());
                // }
            } else {
                logger.info("Topic not matched: " + topic);
                // responseObserver.onNext(Message.MessageResponse.newBuilder().setMessage(request).build());
            }

            Message nmsg = Message.newBuilder()
                    .setId(request.getMessage().getId())
                    .setNode(request.getMessage().getNode())
                    .setFrom(request.getMessage().getFrom())
                    .setTopic(request.getMessage().getTopic())
                    .setPayload(bstr).build();

            ValuedResponse reply = ValuedResponse.newBuilder()
                    .setType(ValuedResponse.ResponsedType.STOP_AND_RETURN)
                    .setMessage(nmsg).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        /**
         * Parse beacon data from byte array
         * 
         * @param data
         * @return
         */
        private Object parseBeaconData(byte[] data) {
            BeaconParser.IBeacon iBeacon = BeaconParser.parseIBeacon(data);
            if (iBeacon != null)
                return iBeacon;

            BeaconParser.EddystoneUID eddystone = BeaconParser.parseEddystoneUID(data);
            if (eddystone != null)
                return eddystone;

            return null;
        }

        // case2: stop publish the 't/d' messages
        // @Override
        // public void onMessagePublish(MessagePublishRequest request,
        // StreamObserver<ValuedResponse> responseObserver) {
        // DEBUG("onMessagePublish", request);
        //
        // Message nmsg = request.getMessage();
        // if ("t/d".equals(nmsg.getTopic())) {
        // ByteString bstr = ByteString.copyFromUtf8("");
        // nmsg = Message.newBuilder()
        // .setId (request.getMessage().getId())
        // .setNode (request.getMessage().getNode())
        // .setFrom (request.getMessage().getFrom())
        // .setTopic (request.getMessage().getTopic())
        // .setPayload(bstr)
        // .putHeaders("allow_publish", "false").build();
        // }
        //
        // ValuedResponse reply = ValuedResponse.newBuilder()
        // .setType(ValuedResponse.ResponsedType.STOP_AND_RETURN)
        // .setMessage(nmsg).build();
        // responseObserver.onNext(reply);
        // responseObserver.onCompleted();
        // }

        @Override
        public void onMessageDelivered(MessageDeliveredRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onMessageDelivered", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onMessageAcked(MessageAckedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onMessageAcked", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onMessageDropped(MessageDroppedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onMessageDropped", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
