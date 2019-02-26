package nl.plff.tictactoe;

public class DataHolder {

    private static DataHolder instance;
    private String username;

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        if (instance == null) instance = new DataHolder();
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
