package exmple;

import java.io.IOException;
import java.net.InetSocketAddress;

import secs.SecsCommunicator;
import secs.SecsException;
import secs.SecsMessage;
import secs.gem.ACKC10;
import secs.gem.CMDA;
import secs.gem.COMMACK;
import secs.gem.ONLACK;
import secs.gem.TIACK;
import secs.hsmsSs.HsmsSsCommunicator;
import secs.hsmsSs.HsmsSsCommunicatorConfig;
import secs.hsmsSs.HsmsSsProtocol;
import secs.secs2.Secs2;
import secs.secs2.Secs2Exception;

public class Example1HsmsSsPassive {

	public Example1HsmsSsPassive() {
		/* Nothing */
	}
	
	public static void main(String[] args) {
		
		HsmsSsCommunicatorConfig config = new HsmsSsCommunicatorConfig();
		
		config.socketAddress(new InetSocketAddress("127.0.0.1", 5000));
		config.protocol(HsmsSsProtocol.PASSIVE);
		config.sessionId(10);
		config.isEquip(true);
		config.notLinktest();
		config.timeout().t3(45.0F);
		config.timeout().t6( 5.0F);
		config.timeout().t7(10.0F);
		config.timeout().t8( 6.0F);
		config.gem().mdln("MDLN-A");
		config.gem().softrev("000001");
		
		try (
				SecsCommunicator comm = HsmsSsCommunicator.newInstance(config);
				) {
			
			comm.addSecsLogListener(System.out::println);
			
			comm.addSecsMessageReceiveListener(msg -> {
				
				try {
					
					if ( msg.sessionId() != config.sessionId() ) {
						comm.gem().s9f1(msg);
						return;
					}
					
					response(comm, msg);
				}
				catch ( InterruptedException ignore ) {
				}
				catch ( SecsException e) {
					e.printStackTrace();
				}
			});
			
			comm.open();
			
			synchronized ( Example1HsmsSsPassive.class ) {
				Example1HsmsSsPassive.class.wait();
			}
		}
		catch ( InterruptedException ignore ) {
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}

	}
	
	private static void response(SecsCommunicator comm, SecsMessage msg)
			throws InterruptedException, SecsException {
		
		int strm = msg.getStream();
		int func = msg.getFunction();
		boolean wbit = msg.wbit();
		Secs2 secs2 = msg.secs2();
		
		try {
			
			switch ( strm ) {
			case 1: {
				
				switch ( func ) {
				case 1: {
					
					if ( wbit ) {
						comm.gem().s1f2(msg);
					}
					break;
				}
				case 13: {
					
					if ( wbit ) {
						comm.gem().s1f14(msg, COMMACK.OK);
					}
					break;
				}
				case 15: {
					
					if ( wbit ) {
						comm.gem().s1f16(msg);
					}
					break;
				}
				case 17: {
					
					if ( wbit ) {
						comm.gem().s1f18(msg, ONLACK.OK);
					}
					break;
				}
				default: {
					
					if ( wbit ) {
						comm.send(msg, strm, 0, false);
					}
					
					comm.gem().s9f5(msg);
				}
				}
				
				break;
			}
			
			case 2: {
				
				switch ( func ) {
				
				case 21: {
					
					String rcmd = secs2.getAscii();
					System.out.println(rcmd);
					
					if ( wbit ) {
						comm.gem().s2f22(msg, CMDA.CommandDoesNotExist);
					}
					break;
				}
				case 31: {
					
					String time = secs2.getAscii();
					System.out.println(time);
					
					if ( wbit ) {
						comm.gem().s2f32(msg, TIACK.OK);
					}
					break;
				}
				default: {
					
					if ( wbit ) {
						comm.send(msg, strm, 0, false);
					}
					
					comm.gem().s9f5(msg);
				}
				}
				
				break;
			}
			
			case 10: {
				
				switch ( func ) {
				case 3: {
					
					/*
					 * Terminal Display, Single-Block
					 * format
					 * S10F3 W
					 * <L
					 *   <B[1] TID>
					 *   <A[n] display-text>
					 * >
					 */
					
					String dispText = secs2.getAscii(1);
					
					System.out.println(dispText);
					
					if ( wbit ) {
						comm.gem().s10f4(msg,ACKC10.AcceptedForDisplay);
					}
					break;
				}
				case 5: {
					
					/*
					 * Terminal Display, Multi-Block
					 * format:
					 * S10F5 W
					 * <L
					 *   <B[1] TID>
					 *   <L[n]
					 *     <A block-1>
					 *     <A block-2>
					 *     ...
					 *   >
					 * >
					 */
					
					Secs2 ss = secs2.get(1);
					
					/* Secs2 implements Iterable */
					for ( Secs2 s : ss ) {
						String dispText = s.getAscii();
						System.out.println(dispText);
					}
					
					if ( wbit ) {
						comm.gem().s10f6(msg, ACKC10.AcceptedForDisplay);
					}
					
					break;
				}
				default: {
					
					if ( wbit ) {
						comm.send(msg, strm, 0, false);
					}
					
					comm.gem().s9f5(msg);
				}
				}
				
				break;
			}
			
			default: {
				
				if ( wbit ) {
					comm.send(msg, strm, 0, false);
				}
				
				comm.gem().s9f3(msg);
			}
			}
		}
		catch ( Secs2Exception e ) {
			
			comm.gem().s9f7(msg);
		}
	}
	
}