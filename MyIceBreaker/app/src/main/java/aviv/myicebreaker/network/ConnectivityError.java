package aviv.myicebreaker.network;

/**
 * Created by Aviv on 03/07/2016.
 */
<<<<<<< HEAD
public class    ConnectivityError {
=======
public class ConnectivityError {
>>>>>>> refs/remotes/origin/Lets-Push
    static final int NO_NETWORK = 0;
    static final int SERVER_ERROR = 1;


    private String message;
    private int id;

    public ConnectivityError(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
