package nl.plff.tictactoe.conn;

import java.io.IOException;
import java.net.Socket;
import nl.plff.plffserver.parcel.Parcel;
import nl.plff.plffserver.parcel.core.HeartbeatParcel;
import nl.plff.plffserver.parcel.core.UnknownObjectParcel;
import nl.plff.plffserver.parcel.processor.ParcelUnit;
import nl.plff.plffserver.server.conn.Connection;

public class ClientConnection extends Connection {

    private final ParcelUnit parcelUnit;

    public ClientConnection(ParcelUnit parcelUnit, Socket socket) throws IOException {
        super(socket);
        this.parcelUnit = parcelUnit;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Parcel p = (Parcel) receiveObject();
                if (p instanceof HeartbeatParcel) {
                    sendParcel(new HeartbeatParcel());
                    // Heartbeat parcels should never make it to the processor and should be handled by the connection itself
                    // This does bake heartbeat into the core of the code but maybe that's a good thing
                    continue;
                }
                // For now, a client is connected to only one server.
                // I guess this can change later but that's out of scope for this project.
                // If, however, I do include it: set sender here and make multiple connections. Similar implementation in ServerConnection.
                parcelUnit.processParcel(p);
            } catch (IOException | ClassNotFoundException | ClassCastException ignored) {
                try {
                    sendParcel(new UnknownObjectParcel());
                } catch (IOException ignored1) {
                    System.err.println("Connection with server failed.");
                    break;
                }
            }
        }
    }
}
