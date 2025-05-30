package mr.demonid.controller;


import mr.demonid.commons.ConnectConfig;
import mr.demonid.commons.Message;
import mr.demonid.view.View;
import mr.demonid.view.listeners.*;

public class Server {

    private final View view;
    private boolean isWorked;
    private Listener connectListener;
    private Thread threadRead;


    public Server(View view) {
        this.view = view;
        isWorked = false;

        setListeners();             // регистрируем слушателей от GUI
    }

    /**
     * Начало работы сервера
     */
    private void startServer()
    {
        if (!isWorked)
        {
            connectListener = new Listener(ConnectConfig.getPort());
            connectListener.start();
            isWorked = true;
            ClientManager.getInstance().removeAllUsers();
            // запускаем отдельный поток чтения и обработки сообщений от пользователей
            threadRead = new Thread(this::readThread);
            threadRead.start();
        }
    }

    /**
     * Приостанавливаем сервер
     */
    private void stopServer()
    {
        if (isWorked)
        {
            ClientManager.getInstance().removeAllUsers();
            threadRead.interrupt();
            connectListener.close();
            isWorked = false;
        }
    }

    /**
     * Поток чтения сообщений от пользователей
     */
    private void readThread()
    {
        try {
            while (!Thread.currentThread().isInterrupted())
            {
                Message message = ClientManager.getInstance().getMessageFromClient();
                if (message.isPrivate())
                {
                    ClientManager.getInstance().resendPrivateMessage(message);
                    view.showMessage(message.getAuthorName() + " to @" + message.getTargetName() + ": " + message.getMessage());
                } else {
                    ClientManager.getInstance().broadcastMessage(message);
                    view.showMessage(message.getAuthorName() + ": " + message.getMessage());
                }
            }
        } catch (InterruptedException ignored) {}
        System.out.println("Server: thread reader stopped.");
    }


    /*===========================================================================
     *
     * Установка/удаление слушателей от View
     *
     ===========================================================================*/
    private void setListeners()
    {
        view.addListener(StartServerListener.class, startServerListener);
        view.addListener(StopServerListener.class, stopServerListener);
        view.addListener(DisconnectListener.class, event -> removeListeners());
    }

    private void removeListeners()
    {
        view.removeListeners(StartServerListener.class, startServerListener);
        view.removeListeners(StopServerListener.class, stopServerListener);
    }


    /*===========================================================================
     *
     * Реализация слушателей от контролов View
     *
     ===========================================================================*/

    private final StartServerListener startServerListener = new StartServerListener() {
        @Override
        public void actionPerformed(StartServerEvent event)
        {
            if (isWorked)
            {
                view.showMessage("Warning: сервер уже запущен.");
            } else {
                view.showMessage("Info: сервер запущен.");
                startServer();
                isWorked = true;
            }
        }
    };

    private final StopServerListener stopServerListener = new StopServerListener() {
        @Override
        public void actionPerformed(StopServerEvent event)
        {
            if (!isWorked)
            {
                view.showMessage("Warning: сервер уже остановлен.");
            } else {
                view.showMessage("Info: сервер остановлен.");
                stopServer();
                isWorked = false;
            }
        }
    };


}
