package example2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;

import secs.SecsException;
import secs.SecsMessage;
import secs.gem.ONLACK;
import secs.hsmsSs.HsmsSsCommunicator;
import secs.hsmsSs.HsmsSsCommunicatorConfig;
import secs.hsmsSs.HsmsSsProtocol;
import secs.secs2.Secs2;
import secs.secs2.Secs2Exception;

public class ExampleHsmsSsActive {

	public ExampleHsmsSsActive() {
		/* Nothing */
	}
	
	/*
	 * Example-2
	 * 1. open ACTIVE-instance
	 * 2. wait until SELECTED
	 * 3. send S1F13
	 * 4. send S1F17
	 * 
	 */
	
	public static void main(String[] args) {
		
		HsmsSsCommunicatorConfig config = new HsmsSsCommunicatorConfig();
		
		config.socketAddress(new InetSocketAddress("127.0.0.1", 5000));
		config.protocol(HsmsSsProtocol.ACTIVE);
		config.sessionId(10);
		config.isEquip(false);
		config.linktest(60.0F);
		config.timeout().t3(45.0F);
		config.timeout().t5(10.0F);
		config.timeout().t6( 5.0F);
		config.timeout().t8( 6.0F);
		
		try (
				HsmsSsCommunicator comm = HsmsSsCommunicator.newInstance(config);
				) {
			
			comm.addSecsLogListener(System.out::println);
			
			comm.addSecsMessageReceiveListener(msg -> {
				
				/* Nothing */
				
			});
			
			comm.addSecsCommunicatableStateChangeListener(state -> {
				
				try {
					
					if ( state /* SELECTED */ ) {
						
						{
							/* build <L[0] > */
							Secs2 ss = Secs2.list();
							
							/* send s1f13 <L[0] > */
							comm.send(1, 13, true, ss);
						}
						
						{
							/* send s1f17 W */
							Optional<SecsMessage> op = comm.send(1, 17, true);
							
							ONLACK onlack = ONLACK.get(op.get().secs2());
							
							System.out.println("ONLACK: " + onlack);
						}
					}
				}
				catch ( InterruptedException ignore ) {
				}
				catch ( SecsException | Secs2Exception e ) {
					e.printStackTrace();
				}
			});
			
			comm.open();
			
			
			synchronized ( ExampleHsmsSsActive.class ) {
				ExampleHsmsSsActive.class.wait();
			}
		}
		catch ( InterruptedException ignore ) {
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		
	}

}
