package mr.demonid.controller;


import mr.demonid.commons.Account;
import mr.demonid.commons.Message;

import java.io.*;
import java.net.Socket;

/**
 * Соединение с клиентом. Работает в отдельном потоке.
 */
public class Client extends Thread {

    private Socket socket;
    private Account account;

    private ObjectInputStream reader;
    private ObjectOutputStream writer;

    public Client(Socket socket) {
        this.socket = socket;
        this.account = init();
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted() && !socket.isClosed())
            {
                Message message = (Message) reader.readObject();
                if (message == null || message.getMessage().isEmpty())
                    break;                                  // на том конце разорвали связь
                ClientManager.getInstance().putMessageFromClient(message);
            }
        } catch (Exception ignored) {
        }
        ClientManager.getInstance().unregisterUser(this);
        closeResource();
    }

    /**
     * Отправка сообщения клиенту
     */
    public void sendMessage(Message message)
    {
        try {
            writer.writeObject(message);
            writer.flush();
        } catch (Exception e)
        {
            ClientManager.getInstance().unregisterUser(this);
            closeResource();
        }
    }

    /**
     * Освобождение занятых клиентом ресурсов (закрытие сокета и потоков)
     */
    public void closeResource()
    {
        closeSocket();
        closeWriter();
        closeReader();
    }

    /**
     * Остановка клиента и освобождение ресурсов.
     */
    public void close()
    {
        interrupt();
        closeResource();
    }

    /**
     * Инициализация клиента: выделение ресурсов и получения данных о пользователе.
     * @return Данные о пользователе, или null в случае ошибки
     */
    private Account init()
    {
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());
            Message msg = (Message) reader.readObject();
            if (msg.getMessage().equalsIgnoreCase("connect"))
            {
                return new Account(msg.getAuthorName(), msg.getAuthorPassword(), "", "");
            }
            throw new IOException("Unexpected body connection message!");
        } catch (Exception e)
        {
            System.out.println("Client error! " + e.getMessage());
            ClientManager.getInstance().unregisterUser(this);
            closeResource();
        }
        return null;
    }

    public Account getAccount() {
        return account;
    }

    public Socket getSocket() {
        return socket;
    }

    /*
        Освобождение ресурсов клиента
     */
    private void closeSocket()
    {
        try {
            socket.close();
        } catch (Exception ignored) {}
    }

    private void closeReader()
    {
        try {
            reader.close();
        } catch (Exception ignored) {}
    }

    private void closeWriter()
    {
        try {
            writer.close();
        } catch (Exception ignored) {}
    }

}
