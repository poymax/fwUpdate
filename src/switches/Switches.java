package switches;

import moves.AutomatedTelnetClient;

import java.io.IOException;
import java.time.LocalTime;

public abstract class Switches {
    String hostName;
    AutomatedTelnetClient telnet;
    public Switches(String address, String login, String password, String hostName) throws IOException {
        this.hostName = hostName;
        telnet = new AutomatedTelnetClient(address);
        telnet.readUntil(":");
        telnet.write(login);
        telnet.readUntil(":");
        telnet.write(password);
        telnet.write("enable");
        telnet.readUntil(hostName + "#");
    }
    public void upgradeBootRoom() throws IOException {
        telnet.readUntil("[Y/N]:");
        telnet.write("Y");
        telnet.readUntil(hostName + "#");
    }
    public void upgradeFirmware() throws IOException, InterruptedException {
        LocalTime currentTime = LocalTime.now();
        telnet.readUntil(hostName + "#");
        telnet.write("reload after "
                + ((currentTime.getMinute() <= 29 ? 28 : 27) - currentTime.getHour()) + ":"
                + ((currentTime.getMinute() <= 29 ? 29 : 89) - currentTime.getMinute()) + ":"
                + (59 - currentTime.getSecond()));
    }
    public void setPathTftp(String typeName) {
    }
}