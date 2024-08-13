package moves;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutomatedTelnetClient {
    private final TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;
    private String address;
    public AutomatedTelnetClient(String address) throws IOException {
        telnet.connect(address, 23);
        this.address = address;
        in = telnet.getInputStream();
        out = new PrintStream(telnet.getOutputStream());
    }
    public void readUntil(String find) throws IOException {
        StringBuilder sb = new StringBuilder();
        char lastChar = find.charAt(find.length() - 1);
        char ch = (char) in.read();
        while (hasConnect(address)) {
            System.out.print(ch);
            sb.append(ch);
            if (ch == lastChar) {
                if (sb.toString().contains(find)) {
                    return;
                }
            } else if (sb.toString().contains("System will be rebooted")) {
                telnet.disconnect();
                return;
            }
            ch = (char) in.read();
        }
        telnet.disconnect();
    }
    public void write(String value) throws IOException {
        out.println(value);
        out.flush();
    }
    public String getBackupImage() throws IOException {
        StringBuilder sb = new StringBuilder();
        String img1 = "image1";
        String img2 = "image2";
        write("show image-info");
        char ch = (char) in.read();
        while (hasConnect(address)) {
            System.out.print(ch);
            sb.append(ch);
            if (sb.toString().contains(img1)) {
                return img2;
            } else if (sb.toString().contains(img2)) {
                return img1;
            }
            ch = (char) in.read();
        }
        telnet.disconnect();
        return null;
    }
    public void disconnect() throws IOException{
        telnet.disconnect();
    }
    public static boolean hasConnect(String address) throws IOException {
        InetAddress ip = InetAddress.getByName(address);
        return ip.isReachable(5000);
    }
}