package io.grpc.helloworldexample;

public class GrpcErrorMessages {
    public static final String MARSHALING_NIL =
      "io.grpc.StatusRuntimeException: INTERNAL: grpc: error while marshaling: proto: " +
      "Marshal called with nil";
    public static final String UNAVAILABLE = "io.grpc.StatusRuntimeException: UNAVAILABLE";
}
