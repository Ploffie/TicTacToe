package nl.plff.tictactoe;

import nl.plff.plffserver.parcel.Parcel;
import nl.plff.plffserver.parcel.core.UnknownObjectParcel;
import nl.plff.plffserver.parcel.core.WelcomeParcel;
import nl.plff.plffserver.parcel.processor.ParcelUnit;
import nl.plff.tictactoe.conn.ClientConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Main {

    private static final String HOST = "www.plff.nl";
    private static final int PORT = 1337;
    private static ClientConnection connection;

    static ClientConnection getConnection() {
        return connection;
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("Connecting to server " + HOST + " on port " + PORT + "...");
        Socket socket;
        try {
            // Open socket to server, or at least try to
            socket = new Socket(InetAddress.getByName(HOST), PORT);
        } catch (IOException e) {
            System.err.println("Failed to connect to server. Did you enter the right address and port?");
            return;
        }
        // Get processing unit for parcels
        ParcelUnit parcelUnit = ParcelUnit.getInstance();
        // Start connection
        try {
            connection = new ClientConnection(parcelUnit, socket);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // Register parcels with handlers
        parcelUnit.registerParcel(WelcomeParcel.class, this::handleWelcomeParcel);
        parcelUnit.registerParcel(UnknownObjectParcel.class, this::handleUnknownObjectParcel);
        connection.start();
    }

    @SuppressWarnings("unused")
    private void handleWelcomeParcel(Parcel ignored) {
        // had to wrap this in thread because javafx launch does not return :angry:
        Executors.newSingleThreadExecutor().execute(UI.getInstance());
    }

    @SuppressWarnings("unused")
    private void handleUnknownObjectParcel(Parcel ignored) {
        System.err.println("Server received unknown parcel.");
    }
}
