package switches;

import java.io.IOException;

public class S5210g extends Switches {
    private final String TFTP = "tftp://10.80.3.161/SNR/S5210/";
    public S5210g(String address, String login, String password, String hostName) throws IOException {
        super(address, login, password, hostName);
    }
    @Override
    public void upgradeBootRoom() throws IOException {
        String BOOTROOM = "boot.rom bootrom";
        telnet.write("copy " + TFTP + BOOTROOM);
        super.upgradeBootRoom();
    }
    public void upgradeFirmware() throws IOException, InterruptedException {
        String FW = "enos.bix file vmlinux.bix";
        telnet.write("copy tftp " + TFTP + FW);
        super.upgradeFirmware();
    }
}
