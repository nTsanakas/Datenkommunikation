package edu.hm.dako.chat.server;

import java.util.concurrent.ExecutorService;

import edu.hm.dako.chat.common.ExceptionHandler;
import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ServerSocketInterface;
import javafx.concurrent.Task;

public class AdvancedChatServerImpl extends SimpleChatServerImpl {

	public AdvancedChatServerImpl(ExecutorService executorService, ServerSocketInterface socket,
			ChatServerGuiInterface serverGuiInterface) {
		super(executorService, socket, serverGuiInterface);
	}
	
	@Override
	public void start() {
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				// Clientliste erzeugen
				clients = SharedChatClientList.getInstance();

				while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
					try {
						// Auf ankommende Verbindungsaufbauwuensche warten
						System.out.println(
								"AdvancedChatServer wartet auf Verbindungsanfragen von Clients...");

						Connection connection = socket.accept();
						log.debug("Neuer Verbindungsaufbauwunsch empfangen");

						// Neuen Workerthread starten
						executorService.submit(new AdvancedChatWorkerThreadImpl(connection, clients,
								counter, serverGuiInterface));
					} catch (Exception e) {
						if (socket.isClosed()) {
							log.debug("Socket wurde geschlossen");
						} else {
							log.error(
									"Exception beim Entgegennehmen von Verbindungsaufbauwuenschen: " + e);
							ExceptionHandler.logException(e);
						}
					}
				}
				return null;
			}
		};

		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
	}

}
