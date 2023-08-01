package io.grpc.helloworldexample;

public class GrpcErrorMessages {
    public static final String MARSHALING_NIL =
      "io.grpc.StatusRuntimeException: INTERNAL: grpc: error while marshaling: proto: " +
      "Marshal called with nil";
    public static final String UNAVAILABLE = "io.grpc.StatusRuntimeException: UNAVAILABLE";

    public static String getRelativeFailedMsg(String rel) {
        switch (rel) {
            case MARSHALING_NIL:
                return "Failed: there's no element with such id.";

            case UNAVAILABLE:
                return "Failed: no connection established.";

            default:
                return "Failed: unknown cause.";
        }
    }
}
