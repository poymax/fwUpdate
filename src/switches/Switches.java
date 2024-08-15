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
    public void upgradeFirmware () throws IOException, InterruptedException {
        LocalTime currentTime = LocalTime.now();
        telnet.readUntil(hostName + "#");
        telnet.write("reload after " + Math.abs(currentTime.getHour() - 27)
                + ":" + (Math.abs(currentTime.getMinute() - 29))
                + ":" + (Math.abs(currentTime.getSecond() - 60)));
        telnet.readUntil(hostName + "#");
        telnet.disconnect();
    }
    public void setPathTftp(String typeName) {
    }
}