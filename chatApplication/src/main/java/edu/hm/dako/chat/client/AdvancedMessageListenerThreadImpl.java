package edu.hm.dako.chat.client;

import edu.hm.dako.chat.common.ChatPDU;
import edu.hm.dako.chat.connection.Connection;

public class AdvancedMessageListenerThreadImpl extends SimpleMessageListenerThreadImpl {

	public AdvancedMessageListenerThreadImpl(ClientUserInterface userInterface, Connection con,
			SharedClientData sharedData) {
		super(userInterface, con, sharedData);
	}
	
	@Override
	protected void chatMessageEventAction(ChatPDU receivedPdu) {
		super.chatMessageEventAction(receivedPdu);
		try {
			connection.send(ChatPDU.createMessageEventResponsePdu(sharedClientData.userName, receivedPdu));
			System.out.println(ChatPDU.createMessageEventResponsePdu(sharedClientData.userName, receivedPdu).toString());
		} catch (Exception e) {
			log.debug("Fehler beim Senden der Bestätigungsnachricht.");
		}
	}

	protected void chatMessageConfirmAction(ChatPDU receivedPdu) {
		sharedClientData.confirmCounter.getAndIncrement();
		userInterface.readConfirm();
	}
	
	@Override
	public void run() {

		ChatPDU receivedPdu = null;

		log.debug("AdvancedMessageListenerThread gestartet");
		log.info("AdvancedMessageListenerThread gestartet");

		while (!finished) {

			try {
				// Naechste ankommende Nachricht empfangen
				log.debug("Auf die naechste Nachricht vom Server warten");
				receivedPdu = receive();
				log.debug("Nach receive Aufruf, ankommende PDU mit PduType = "
						+ receivedPdu.getPduType());
			} catch (Exception e) {
				finished = true;
			}

			if (receivedPdu != null) {

				switch (sharedClientData.status) {

				case REGISTERING:

					switch (receivedPdu.getPduType()) {

					case LOGIN_RESPONSE:
						// Login-Bestaetigung vom Server angekommen
						loginResponseAction(receivedPdu);
						break;

					case LOGIN_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User erweitert hat
						loginEventAction(receivedPdu);
						break;

					case LOGOUT_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User veraendert hat
						logoutEventAction(receivedPdu);
						break;

					case CHAT_MESSAGE_EVENT:
						// Chat-Nachricht vom Server gesendet
						chatMessageEventAction(receivedPdu);
						break;
						
					case CHAT_MESSAGE_CONFIRM:
						// Chat-Nachricht von allen Clients gelesen
						chatMessageConfirmAction(receivedPdu);
						break;

					default:
						log.debug("Ankommende PDU im Zustand " + sharedClientData.status
								+ " wird verworfen");
					}
					break;

				case REGISTERED:

					switch (receivedPdu.getPduType()) {

					case CHAT_MESSAGE_RESPONSE:

						// Die eigene zuletzt gesendete Chat-Nachricht wird vom
						// Server bestaetigt.
						chatMessageResponseAction(receivedPdu);
						break;

					case CHAT_MESSAGE_EVENT:
						// Chat-Nachricht vom Server gesendet
						chatMessageEventAction(receivedPdu);
						break;
						
					case CHAT_MESSAGE_CONFIRM:
						// Chat-Nachricht von allen Clients gelesen
						chatMessageConfirmAction(receivedPdu);
						break;

					case LOGIN_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User erweitert hat
						loginEventAction(receivedPdu);

						break;

					case LOGOUT_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User veraendert hat
						logoutEventAction(receivedPdu);

						break;

					default:
						log.debug("Ankommende PDU im Zustand " + sharedClientData.status
								+ " wird verworfen");
					}
					break;

				case UNREGISTERING:

					switch (receivedPdu.getPduType()) {

					case CHAT_MESSAGE_EVENT:
						// Chat-Nachricht vom Server gesendet
						chatMessageEventAction(receivedPdu);
						break;

					case LOGOUT_RESPONSE:
						// Bestaetigung des eigenen Logout
						logoutResponseAction(receivedPdu);
						break;

					case LOGIN_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User erweitert hat
						loginEventAction(receivedPdu);

						break;

					case LOGOUT_EVENT:
						// Meldung vom Server, dass sich die Liste der
						// angemeldeten User veraendert hat
						logoutEventAction(receivedPdu);

						break;

					default:
						log.debug("Ankommende PDU im Zustand " + sharedClientData.status
								+ " wird verworfen");
						break;
					}
					break;

				case UNREGISTERED:
					log.debug(
							"Ankommende PDU im Zustand " + sharedClientData.status + " wird verworfen");

					break;

				default:
					log.debug("Unzulaessiger Zustand " + sharedClientData.status);
				}
			}
		}
	}
}
