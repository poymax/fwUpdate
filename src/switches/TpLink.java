package switches;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

public class TpLink extends Switches {
    private final String TFTP = "/TPL/TL-SG3428X-UPS/";
    private final String FW = "Firmware.bin";
    private final String HOSTNAME;
    public TpLink(String address, String login, String password, String hostName) throws IOException {
        super(address, login, password, hostName);
        this.HOSTNAME = hostName;
    }
    public void upgradeFirmware() throws IOException {
        telnet.write("firmware upgrade ip-address 10.80.3.161 filename " + TFTP + FW);
        telnet.readUntil("(Y/N):");
        telnet.write("Y");
        telnet.readUntil("(Y/N):");
        telnet.write("N");
        telnet.readUntil(HOSTNAME + "#");
        telnet.write("configure");
        telnet.readUntil(HOSTNAME + "(config)#");
        telnet.write("boot application filename " +  telnet.getBackupImage() + " startup");
        telnet.readUntil(HOSTNAME + "(config)#");
        telnet.write("reboot-schedule at 04:30 save_before_reboot");
        telnet.readUntil(HOSTNAME + "(config)#");
        telnet.readUntil("(Y/N):");
        telnet.write("Y");
        telnet.readUntil(HOSTNAME + "(config)#");
        telnet.write("exit");
        telnet.readUntil(HOSTNAME + "#");
        telnet.write("copy running-config startup-config");
        telnet.readUntil(HOSTNAME + "#");
        telnet.write("exit");
    }
}
