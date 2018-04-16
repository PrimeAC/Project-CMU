package pt.ulisboa.tecnico.cmu.response;

public class TicketResponse implements Response {

    private static final long serialVersionUID = 734457624276534179L;
    private String message;

    public TicketResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
