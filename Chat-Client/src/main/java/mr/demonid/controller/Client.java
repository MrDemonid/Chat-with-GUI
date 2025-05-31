package mr.demonid.controller;


import mr.demonid.commons.Account;
import mr.demonid.commons.ConnectStatus;
import mr.demonid.commons.Message;
import mr.demonid.view.View;
import mr.demonid.view.listeners.DisconnectListener;
import mr.demonid.view.listeners.LoginListener;
import mr.demonid.view.listeners.SendMessageListener;

import java.io.*;
import java.net.Socket;

public class Client {

    private Account account;
    private Socket socket;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    private Thread threadRead;

    View view;
    ConnectStatus connectStatus;


    public Client(View view) {
        this.account = new Account("Anon", "12345", "localhost", "4310");
        this.view = view;
        connectStatus = ConnectStatus.DISCONNECTED;
        view.setAccount(account);
        setListeners();

        // Вешаем обработчик на закрытие программы
        Runtime.getRuntime().addShutdownHook(new Thread(this::removeListeners));
    }

    /**
     * Регистрация слушателей от View
     */
    private void setListeners()
    {
        view.addListener(LoginListener.class, event -> connectToServer());
        view.addListener(SendMessageListener.class, event -> sendMessage(event.getMessage()));
        view.addListener(DisconnectListener.class, event -> disconnectFromUser());
    }

    private void removeListeners()
    {
        view.removeListeners(LoginListener.class, event -> connectToServer());
        view.removeListeners(DisconnectListener.class, event -> disconnectFromUser());
    }

    /**
     * Коннектимся к серверу
     */
    private void connectToServer()
    {
        account = view.getAccount();
        String ip = account.getIp();
        String port = account.getPort();
        if ((port.length() > 3 && port.length() < 6 && port.matches("(\\d*\\.)?\\d+"))
                && (ip.equalsIgnoreCase("localhost") || ip.matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)(\\.(?!$)|$)){4}$")))
        {
            if (connectStatus == ConnectStatus.DISCONNECTED)
            {
                view.showMessage("Connect...");
                threadRead = new Thread(this::readerThread);
                threadRead.start();
            } else {
                // что-то непонятно, сюда не должны попасть из-за отсутствия панели подключения
                view.errorMessage("Error: unexpected error! Please restart program!");
            }
        } else {
            view.errorMessage("Bad ip or port format!");
        }

    }

    /**
     * Пользователь сам прерывает сеанс
     */
    private void disconnectFromUser()
    {
        close();
        view.setConnectStatus(connectStatus);
        System.out.println("Disconnect");
    }

    /**
     * Отправка сообщения на сервер
     * @param message Сообщение (если начинается с @имя, то это личное, отправляется только адресату)
     */
    public void sendMessage(String message) {
        if (message != null && !message.isEmpty())
        {
            String to = null;
            if (message.charAt(0) == '@')
            {
                // извлекаем имя адресата
                to = getTargetName(message);
                message = getBodyMessage(to, message);
            }
            Message msg = new Message(account, to, message);
            try {
                writer.writeObject(msg);
                writer.flush();
            } catch (Exception e)
            {
                close();
            }
        }
    }

    /**
     * Освобождение занятых ресурсов
     */
    private void close() {
        closeSocket();
        closeReader();
        closeWriter();
    }

    /**
     * Поток чтения сообщений от сервера.
     */
    private void readerThread()
    {
        if (!connect(account))
        {
            switchConnectedStatus(ConnectStatus.DISCONNECTED, "Connect error: server not found!");
            return;
        }
        switchConnectedStatus(ConnectStatus.CONNECTED, "\u001B[30mConnected!");
        try
        {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed())
            {
                Message message = (Message) reader.readObject();
                if (message == null)
                    break;                                      // потеря связи с сервером
                view.innerMessage(message);
            }
        } catch (Exception ignored) {}
        switchConnectedStatus(ConnectStatus.DISCONNECTED, "Connect closed.");
        close();
    }

    /**
     * Переключение статуса клиента. Версия для отдельного от Swing потока.
     * @param message Сообщение пользователю.
     */
    private void switchConnectedStatus(ConnectStatus newStatus, String message)
    {
        connectStatus = newStatus;
        view.setConnectStatus(connectStatus);
        if (connectStatus == ConnectStatus.DISCONNECTED)
            view.errorMessage(message);
        else
            view.showMessage(message);
    }

    /**
     * Попытка связаться с сервером
     * @param account Аккаунт клиента, с данными об IP сервера и его порте.
     * @return true - если соединение прошло успешно.
     */
    private boolean connect(Account account)
    {
        try
        {
            int port = Integer.parseInt(account.getPort());
            String ip = account.getIp();
            socket = new Socket(ip, port);
            writer = new ObjectOutputStream(socket.getOutputStream());
            reader = new ObjectInputStream(socket.getInputStream());
            sendMessage("connect");
        } catch (Exception e)
        {
            close();        // закрываем возможно открытые ресурсы
            return false;
        }
        return true;
    }

    /**
     * Извлекает имя пользователя из личного сообщения
     * @param message Личное сообщение, начинающееся на @имя ...
     */
    private String getTargetName(String message)
    {
        StringBuilder name = new StringBuilder();
        for (int i = 1; i < message.length(); i++)
        {
            char ch = message.charAt(i);
            if (Character.isWhitespace(ch))
                return name.toString();
            name.append(ch);
        }
        return name.toString();
    }

    /**
     * Извлекает тело сообщения из личного сообщения
     */
    private String getBodyMessage(String name, String source)
    {
        return source.replaceFirst("@" + name, "").trim();
    }

    /*
        Методы освобождения ресурсов.
        Реализованы по отдельности, поскольку если их сделать в одном блоке try catch,
        то первое исключение оставит следующие за ним ресурсы без close()
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
