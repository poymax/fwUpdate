import com.google.gson.*;
import com.sun.jdi.VMDisconnectedException;
import netscape.javascript.JSObject;
import switches.*;
import moves.SnmpWalk;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        final String SWITCHEXCEPTION = "10.50.0.2";
        final String OIDSNRMODEL = "1.3.6.1.2.1.1.5.0";
        final String OIDSNR1BOOTROOM = "1.3.6.1.2.1.47.1.1.1.1.9.1";
        final String OIDSNR1FW = "1.3.6.1.4.1.40418.7.100.1.3.0";
        final String OIDSNR10FW = "1.3.6.1.2.1.47.1.1.1.1.10.1";
        final String OIDTPLMODEL = "1.3.6.1.4.1.11863.6.1.1.2.0";
        final String OIDTPLFW = "1.3.6.1.4.1.11863.6.1.1.6.0";
        final String OIDHOSTNAME = "1.3.6.1.2.1.1.5.0";
        String regexPoe8x = "SNR-S298(2|5)G-24T-POE";
        final int SNR65 = 16;
        final int[] SNR8xG = {19, 20, 21};
        final int SNR8xPOE = 19;
        final int SNR85GU = 20;
        final int SNR85G8P = 21;
        final int TPL = 25;
        final int SNR10G = 26;
        final int bootRoomVersion = 7240;
        String currentVersion = null;
        SnmpWalk snmpWalk;
        String dateFormat = "yy-MM-dd HH-mm-ss";
        String timeFormat = "HH:mm:ss";
        String projectDir = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (projectDir.matches("(.*).jar")) {
            projectDir = projectDir.substring(0, projectDir.lastIndexOf("/") + 1);
        }
        Path pathFile = Paths.get(projectDir + "SwitchesUpdater/switches.json");
        Path pathLogDir = Paths.get(projectDir + "SwitchesUpdater/Log");
        Path pathActualVer = Paths.get(projectDir + "SwitchesUpdater/Actual Version.txt");
        String actualVerSnr10 = null;
        String actualVerSnr = null;
        String actualVerTpl = null;
        int count = 1;
        int countUpdate = 0;
        int countUpdateLimit = 300;
        LocalDateTime fileTime = LocalDateTime.now();
        if (!Files.isDirectory(pathLogDir) || !Files.exists(pathLogDir)) {
            Files.createDirectories(pathLogDir);
        }
        PrintWriter logFile = new PrintWriter(pathLogDir + "/switches " + fileTime
                .format(DateTimeFormatter.ofPattern(dateFormat)) + ".txt");
        FileWriter writer;
        //==================================================================================
        BufferedReader reader = new BufferedReader(new FileReader(pathActualVer.toFile()));
        actualVerSnr = reader.readLine();
        actualVerSnr10 = reader.readLine();
        actualVerTpl = reader.readLine();
        reader.close();
        System.out.println("Актуальная версия SNR 1G:  " + actualVerSnr
                + "\nАктуальная версия SNR 10G: " + actualVerSnr10
                + "\nАктуальная версия TPLINK:  " + actualVerTpl);
        Thread.sleep(5000);
        //==================================================================================
        JsonArray jsonArray = JsonParser.parseReader(Files.newBufferedReader(pathFile)).getAsJsonArray();
        for (JsonElement jsonElement : jsonArray) {
            String address = jsonElement.getAsJsonObject().get("l2_sw_ip").getAsString();
            String login = jsonElement.getAsJsonObject().get("l2_sw_login").getAsString();
            String password = jsonElement.getAsJsonObject().get("l2_sw_pass").getAsString();
            String community = jsonElement.getAsJsonObject().get("community").getAsString();
            String typeName = jsonElement.getAsJsonObject().get("sw_type_name").getAsString();
            int type = jsonElement.getAsJsonObject().get("sw_type_id").getAsInt();
            if (address.equalsIgnoreCase(SWITCHEXCEPTION)) {
                continue;
            }
            snmpWalk = new SnmpWalk();
            StringBuilder builder = new StringBuilder();
            System.out.println("\n\n===============Коммутатор №" + count + "===============");
            System.out.println(address + " - " + typeName);
            count++;
            //==================================================================================
            if (hasConnect(address)) {
                while (hasConnect(address)) {
                    String realTypeName = null;
                    String actualVersion = null;
                    switch (type) {
                        case SNR65, SNR85GU, SNR85G8P, SNR10G
                            -> realTypeName = snmpWalk.getVariable(community, address, OIDSNRMODEL);
                        case TPL -> realTypeName = snmpWalk.getVariable(community, address, OIDTPLMODEL);
                    }
                    LocalTime currentTime = LocalTime.now();
                    builder.append(address)
                            .append("\t\t-\t")
                            .append(typeName + "\n");
                    try {
                        if (type == SNR8xPOE && realTypeName.matches(regexPoe8x)) {
                            typeName = realTypeName;
                        } else if (!realTypeName.equalsIgnoreCase(typeName)) {
                            builder.append("\tMODEL INCORRECT!\n");
                            logFile.write(builder.toString());
                            logFile.flush();
                            break;
                        }
                    } catch (NullPointerException ex) {
                        logFile.write(builder.toString());
                        logFile.flush();
                        break;
                    }
                    //===================Get Current Version SNR or TpLink============================
                    switch (type) {
                    case SNR65, SNR8xPOE, SNR85GU, SNR85G8P
                        -> {
                        currentVersion = snmpWalk.getVariable(community, address, OIDSNR1FW);
                        actualVersion = actualVerSnr;
                    }
                    case SNR10G -> {
                        currentVersion = snmpWalk.getVariable(community, address, OIDSNR10FW);
                        actualVersion = actualVerSnr10;
                    }
                        case TPL -> {
                            currentVersion = snmpWalk.getVariable(community, address, OIDTPLFW);
                            actualVersion = actualVerTpl;
                        }
                    }
                    //================================================================================
                    builder.append("\t" + currentVersion);
                    System.out.println("Current version: " + currentVersion);
                    if (currentVersion == null) {
                        builder.append(";\n");
                        logFile.write(builder.toString());
                        logFile.flush();
                        break;
                    }
                    String hostName = snmpWalk.getVariable(community, address, OIDHOSTNAME);
                    Switches aSwitch = null;
                    //================================SWITCH MODEL===================================
                    if (!actualVersion(currentVersion, actualVersion)) {
                        switch (type) {
                            case SNR65, SNR8xPOE, SNR85GU, SNR85G8P
                                    -> aSwitch = new S29xx(address, login, password, hostName, typeName);
                            case SNR10G -> aSwitch = new S5210g(address, login, password, hostName);
                            case TPL -> aSwitch = new TpLink(address, login, password, hostName);
                        }
                    } else {
                        builder.append("\n");
                        logFile.write(builder.toString());
                        logFile.flush();
                        break;
                    }
                    //================================SWITCH UPDATE===================================
                    try {
                        LocalTime before = LocalTime.now();
                        aSwitch.upgradeFirmware();
                        currentTime = LocalTime.now();
                        countUpdate++;
                        builder.append("\t-->\t")
                                .append(actualVersion)
                                .append(";\n\t")
                                .append(currentTime.format(DateTimeFormatter.ofPattern(timeFormat)))
                                .append(".\tПрошит: " + countUpdate)
                                .append(" коммутатор;\n");
                        logFile.write(builder.toString());
                        logFile.flush();
                        System.out.println("\n\nВремя залития прошивки: "
                                + before.until(currentTime, ChronoUnit.MINUTES) + " мин "
                                + before.until(currentTime, ChronoUnit.SECONDS) % 60 + " сек");
                        System.out.println("Прошито коммутаторов: " + countUpdate + " из " + countUpdateLimit);
                    } catch (NullPointerException ex) {
                        builder.append(";\n\tupdate is break!\n");
                        logFile.write(builder.toString());
                        logFile.flush();
                    }
                    break;
                }
                if (countUpdate == countUpdateLimit) {
                    break;
                };
            } else {
                builder.append(address)
                        .append(" - \tThe host is not available!\n");
                logFile.write(builder.toString());
                logFile.flush();
            }
        }
        logFile.close();
    }
    public static boolean hasConnect(String address) throws IOException {
        InetAddress ip = InetAddress.getByName(address);
         return ip.isReachable(5000) ? true : false;
    }
    public static boolean actualVersion (String current, String update) {
        if (current.length() != update.length()) {
            return false;
        } else {
            current = current.replaceAll("\\D", "");
            update = update.replaceAll("\\D", "");
            for (int i = 0; i < (current.length()); i++) {
                if (Integer.parseInt(String.valueOf(current.charAt(i)))
                        < Integer.parseInt(String.valueOf(update.charAt(i)))) {
                    return false;
                }
            }
            return true;
        }
    }
}