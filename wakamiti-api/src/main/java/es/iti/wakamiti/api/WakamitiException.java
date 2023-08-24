package es.iti.wakamiti.api;

public class WakamitiException extends RuntimeException {

    String unformattedMessage;
    Object[] messageArguments;

    public WakamitiException(String message) {
        super(message);
        this.unformattedMessage = message;
        this.messageArguments = new Object[0];
    }

    public WakamitiException(String message, Object... messageArguments) {
        super(format(message,messageArguments));
        this.unformattedMessage = message;
        this.messageArguments = messageArguments;
    }

    public WakamitiException(Exception e, String message, Object... messageArguments) {
        super(format(message,messageArguments)+": "+e.getMessage(),e);
        this.unformattedMessage = message +": "+e.getMessage();
        this.messageArguments = messageArguments;
    }


    public WakamitiException(Exception e) {
        super(e.getMessage(),e);
        this.unformattedMessage = e.getMessage();
        this.messageArguments = new Object[0];
    }


    private static String format(String message, Object[] args) {
        return message.replaceAll("\\{[^}]*}","%s").formatted(args);
    }

}
