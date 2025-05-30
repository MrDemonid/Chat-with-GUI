package mr.demonid.controller;


import mr.demonid.commons.Account;
import mr.demonid.commons.Message;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Менеджер подключений клиентов.
 * Для него скорость не критична, поэтому в целях потокобезопасности
 * применяется простой synchronized(clients).
 * Хотя это скорее подстраховка на случай ввода слушателей для
 * нескольких портов.
 * (кроме удаления, которое могут запросить несколько пользователей сразу).
 * Реализован как синглтон.
 */
public class ClientManager {

    private static final ClientManager instance = new ClientManager();

    private final List<Client> clients = new ArrayList<>();
    private final ArrayBlockingQueue<Message> fromClients = new ArrayBlockingQueue<>(1000);


    private ClientManager() {}

    public static ClientManager getInstance() {
        return instance;
    }


    /**
     * Блокирующая потокобезопасная функция выборки сообщений от клиентов
     * @return
     */
    public Message getMessageFromClient() throws InterruptedException {
        return fromClients.take();
    }

    /**
     * Потокобезопасное помещение сообщения от клиента в очередь
     * @return true - если сообщение удалось поместить в очередь, false - в случае переполненной очереди
     */
    public boolean putMessageFromClient(Message message) {
        return fromClients.offer(message);
    }

    /**
     * Рассылка сообщения всем подключенным клиентам, кроме автора сообщения
     */
    public void broadcastMessage(Message message)
    {
        clients.forEach(e -> {
            if (!e.getAccount().getName().equals(message.getAuthorName()))
                e.sendMessage(message);
        });
    }

    /**
     * Пересылка приватного сообщения адресату
     */
    public void resendPrivateMessage(Message message)
    {
        for (Client client : clients) {
            if (client.getAccount().getName().equals(message.getTargetName()))
            {
                client.sendMessage(message);
                break;
            }
        }
    }

    /**
     * Добавляем нового пользователя, в отдельный поток
     */
    public void addUser(Socket socket)
    {
        boolean found = clients.stream().anyMatch(e -> e.getSocket().equals(socket));
        if (!found)
        {
            Client client = new Client(socket);
            synchronized (clients) {
                clients.add(client);
            }
            client.start();
            // оповещаем сервер о новом подключении
            putMessageFromClient(new Message(client.getAccount(), null, "присоединился к нам"));
        }
    }

    /**
     * Удаляет клиента из списка
     */
    public void unregisterUser(Client client)
    {
        synchronized (clients) {
            if (clients.remove(client))
            {
                Account cl = client.getAccount();
                putMessageFromClient(new Message(cl, null, "покинул нас!"));
            }
        }
    }

    public void removeAllUsers()
    {
        boolean isClear;
        synchronized (clients) {
            isClear = !clients.isEmpty();
            while (!clients.isEmpty())
            {
                Client client = clients.removeLast();
                client.close();
            }
        }
        fromClients.clear();
        if (isClear)
            putMessageFromClient(new Message(new Account("ClientManager", "", "", ""), null, "Все клиенты отключены!"));
    }

}
