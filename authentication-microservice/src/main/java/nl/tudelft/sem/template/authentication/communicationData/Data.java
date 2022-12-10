package nl.tudelft.sem.template.authentication.communicationData;

public class Data {

    public String date;
    public String state;
    public String message;

    public Data(String date, String state, String message) {
        this.date = date;
        this.state = state;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Data{" +
                "date='" + date + '\'' +
                ", state='" + state + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
