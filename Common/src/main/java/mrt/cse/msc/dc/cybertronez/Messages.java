package mrt.cse.msc.dc.cybertronez;

public enum Messages {
    REG("REG"), REGOK("REGOK"), UNREG("UNREG"), UNROK("UNROK"), ECHO("ECHO"), ECHOK("ECHOK"), JOIN("JOIN"),
    JOINOK("JOINOK"), LEAVE("LEAVE"), LEAVEOK("LEAVEOK"), SER("SER"), SEROK("SEROK"), ERROR("ERROR"), CODE0("0"),
    CODE9999("9999"), CODE9998("9998"), CODE9997("9997"), CODE9996("9996"), DETAILS("DETAILS"),
    ALREADY_SEARCHED("ALREADY_SEARCHED"), NOT_FOUND("NOT_FOUND"), PING("PING"), PING_OK("PING_OK"),
    SEND_LEAVE("SEND_LEAVE"), SEND_LEAVE_OK("SEND_LEAVE_OK");

    private String value;

    Messages(final String value) {

        this.value = value;
    }

    public String getValue() {

        return value;
    }

    @Override
    public String toString() {

        return getValue();
    }
}
