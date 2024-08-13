package moves;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
public class SnmpWalk {
    private final CommunityTarget<Address> TARGET;
    public SnmpWalk () {
        TARGET = new CommunityTarget<>();
        TARGET.setVersion(SnmpConstants.version2c);
    }
    public String getVariable(String community, String address, String OID) throws IOException, InterruptedException {
        org.snmp4j.smi.OID oid = new OID(OID);
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);
        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
        snmp.listen();
        int PORT = 161;
        Address targetAddress = new UdpAddress(address + "/" + PORT);
        TARGET.setCommunity(new OctetString(community));
        TARGET.setAddress(targetAddress);
        ResponseEvent<Address> responseEvent = snmp.send(pdu, TARGET);
        PDU response = responseEvent.getResponse();
        String result = null;
        for (int i = 0; i < 5; i++) {
            if (response != null) {
                result = response.get(0).getVariable().toString();
                break;
            }
        }
        snmp.close();
        return result;
    }
}