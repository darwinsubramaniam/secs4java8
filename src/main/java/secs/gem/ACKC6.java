package secs.gem;

import secs.secs2.Secs2;
import secs.secs2.Secs2Exception;

public enum ACKC6 {
	
	UNDEFINED((byte)0xFF),
	
	OK((byte)0x0),
	
	;
	
	private final byte code;
	private final Secs2 ss;
	
	private ACKC6(byte b) {
		this.code = b;
		this.ss = Secs2.binary(b);
	}
	
	public byte code() {
		return code;
	}
	
	public Secs2 secs2() {
		return ss;
	}
	
	public static ACKC6 get(byte b) {
		
		for ( ACKC6 v : values() ) {
			if ( v == UNDEFINED ) continue;
			if ( v.code == b ) {
				return v;
			}
		}
		
		return UNDEFINED;
	}
	
	public static ACKC6 get(Secs2 value) throws Secs2Exception {
		byte b = value.getByte(0);
		return get(b);
	}
}
