package switches;

import java.io.IOException;

public class S29xx extends Switches {
    final String BOOTROOM = "boot.rom boot.rom";
    private final String FW = "nos.img nos.img";
    private String tftp = "tftp://10.80.3.161/SNR/SNR-S2965/";
    public S29xx(String address, String login, String password, String hostName) throws IOException {
        super(address, login, password, hostName);
    }
    @Override
    public void upgradeBootRoom() throws IOException {
        telnet.write("copy " + tftp + BOOTROOM);
        super.upgradeBootRoom();
    }
    public void upgradeFirmware() throws IOException, InterruptedException {
        telnet.write("copy " + tftp + FW);
        telnet.readUntil("[Y/N]:");
        telnet.write("Y");
        super.upgradeFirmware();
    }
    public void setPathTftp(String typeName) {
        String TFTP2965 = "tftp://10.80.3.161/SNR/SNR-S2965/";
        String TFTP2982 = "tftp://10.80.3.161/SNR/SNR-S2982/";
        String TFTP2985 = "tftp://10.80.3.161/SNR/SNR-S2985/";
        if (typeName.equalsIgnoreCase(Model.S2965.getTypeName())) {
            tftp = TFTP2965;
        } else if (typeName.equalsIgnoreCase(Model.S2982G_POE.getTypeName())
                || typeName.equalsIgnoreCase(Model.S2982G_POE_E.getTypeName())) {
            tftp = TFTP2982;
        } else tftp = TFTP2985;
    }
}