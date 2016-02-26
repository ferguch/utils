import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class CIDRUtils {

	private final String cidr;
    //private InetAddress inetAddress;
    private InetAddress startAddress;
    private InetAddress endAddress;
    //private final int prefixLength;
    private BigInteger startRawAddress;
    private BigInteger endRawAddress;


    public CIDRUtils(String cidr) throws UnknownHostException {

        this.cidr = cidr;

        // split CIDR to address and prefix part
        if (this.cidr.contains("/")) {
            int index = this.cidr.indexOf("/");
            String addressPart = this.cidr.substring(0, index);
            String networkPart = this.cidr.substring(index + 1);

            InetAddress inetAddress = InetAddress.getByName(addressPart);
            int prefixLength = Integer.parseInt(networkPart);

            ByteBuffer maskBuffer;
            int targetSize;
            if (inetAddress.getAddress().length == 4) {
                maskBuffer =
                        ByteBuffer
                                .allocate(4)
                                .putInt(-1);
                targetSize = 4;
            } else {
                maskBuffer = ByteBuffer.allocate(16)
                        .putLong(-1L)
                        .putLong(-1L);
                targetSize = 16;
            }

            final BigInteger mask = (new BigInteger(1, maskBuffer.array())).not().shiftRight(prefixLength);
            BigInteger mask2 = (new BigInteger(1, maskBuffer.array())).not().shiftRight(prefixLength);

            ByteBuffer buffer = ByteBuffer.wrap(inetAddress.getAddress());
            BigInteger ipVal = new BigInteger(1, buffer.array());

            startRawAddress = ipVal.and(mask);
            endRawAddress = startRawAddress.add(mask.not());

            byte[] startIpArr = toBytes(startRawAddress.toByteArray(), targetSize);
            byte[] endIpArr = toBytes(endRawAddress.toByteArray(), targetSize);

            startAddress = InetAddress.getByAddress(startIpArr);
            endAddress = InetAddress.getByAddress(endIpArr);
        } else {
            throw new IllegalArgumentException("not an valid CIDR format!");
        }
    }

    private byte[] toBytes(byte[] array, int targetSize) {
        int counter = 0;
        List<Byte> newArr = new ArrayList<Byte>();
        while (counter < targetSize && (array.length - 1 - counter >= 0)) {
            newArr.add(0, array[array.length - 1 - counter]);
            counter++;
        }

        int size = newArr.size();
        for (int i = 0; i < (targetSize - size); i++) {

            newArr.add(0, (byte) 0);
        }

        byte[] ret = new byte[newArr.size()];
        for (int i = 0; i < newArr.size(); i++) {
            ret[i] = newArr.get(i);
        }
        return ret;
    }

    public String getNetworkAddress() {
        return this.startAddress.getHostAddress();
    }
    
    public BigInteger getNumNetworkAddress() {
        return startRawAddress;
    }
    
    public BigInteger getNumBroadcastAddress() {
        return endRawAddress;
    }

    public String getBroadcastAddress() {
        return this.endAddress.getHostAddress();
    }

    @Override
    public String toString()
    {
    	return "CIDR: " + cidr + ", Range: " + getNetworkAddress() + " (" + getNumNetworkAddress() + ") -> " + getBroadcastAddress() + " (" + getNumBroadcastAddress() + ")";
    }
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("Start");
		
		CIDRUtils cidr = new CIDRUtils("165.120.61.127/30");
		System.out.println(cidr.toString());
		CIDRUtils cidr2 = new CIDRUtils("165.120.61.127/16");
		System.out.println(cidr2.toString());
		System.out.println("End");
	}
}