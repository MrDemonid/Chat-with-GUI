package mr.demonid.view;


import mr.demonid.commons.Account;
import mr.demonid.commons.ConnectStatus;
import mr.demonid.view.listeners.*;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class ViewSwing extends JFrame implements View {

    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 600;
    private static final int WINDOW_POS_X = 300;
    private static final int WINDOW_POS_Y = 0;

    JPanel controlPanel;
    JTextArea historyPane;
    JTextField inpIP;
    JTextField inpPort;
    JTextField inpName;
    JPasswordField inpPassword;
    JButton btnLogin;

    JTextField inpMessage;
    JButton btnSend;

    ConnectStatus connectStatus;

    private final EventListenerList listenerList;

    public ViewSwing() throws HeadlessException {
        listenerList = new EventListenerList();
        connectStatus = ConnectStatus.DISCONNECTED;
        init();
        setVisible(true);
    }

    @Override
    public void showMessage(String message) {
        historyPane.append(message);
        historyPane.append("\n");
    }

    @Override
    public void setConnectStatus(ConnectStatus status) {
        connectStatus = status;
        switchConnectStatus();
    }

    @Override
    public void setAccount(Account account) {
        inpName.setText(account.getName());
        inpIP.setText(account.getIp());
        inpPort.setText(account.getPort());
        inpPassword.setText(account.getPassword());
    }

    @Override
    public Account getAccount() {
        return new Account(inpName.getText(), Arrays.toString(inpPassword.getPassword()),
                inpIP.getText(), inpPort.getText());
    }

    @Override
    public <T extends ClientEventListener> void addListener(Class<T> t, T l) {
        listenerList.add(t, l);
    }

    @Override
    public <T extends ClientEventListener> void removeListeners(Class<T> t, T l) {
        listenerList.remove(t, l);
    }

    /**
     * Рассылка уведомлений о закрытии окна пользователем
     */
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            fireDisconnect(new DisconnectEvent(e.getSource()));
            removeListeners();
        }

    }

    /**
     * Меняет статус окна клиента (скрывая/показывая хидер)
     */
    private void switchConnectStatus()
    {
        if (connectStatus == ConnectStatus.CONNECTED)
        {
            controlPanel.setVisible(false);
            historyPane.setText("");
        }
        else
            controlPanel.setVisible(true);
    }

    private void init()
    {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Client");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocation(WINDOW_POS_X, WINDOW_POS_Y);
        createPanels();
        setListeners();
    }

    /**
     * Установка слушателей на контролы
     */
    private void setListeners()
    {
        btnLogin.addActionListener(e -> fireLogin(new LoginEvent(e.getSource())));

        btnSend.addActionListener(e -> {
            showMessage(inpMessage.getText());
            fireSendMessage(new SendMessageEvent(e.getSource(), inpMessage.getText()));
            inpMessage.setText("");
        });

        inpMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    showMessage(inpMessage.getText());
                    fireSendMessage(new SendMessageEvent(e.getSource(), inpMessage.getText()));
                    inpMessage.setText("");
                }
            }
        });
    }

    /**
     * Удаление всех слушателей контролов
     */
    private void removeListeners()
    {
        ActionListener[] list = btnLogin.getActionListeners();
        for (ActionListener listener : list) {
            btnLogin.removeActionListener(listener);
        }

        list = btnSend.getActionListeners();
        for (ActionListener listener: list) {
            btnSend.removeActionListener(listener);
        }

        list = inpMessage.getActionListeners();
        for (ActionListener listener : list) {
            inpMessage.removeActionListener(listener);
        }
    }

    /*===========================================================================
     *
     * Создание граф. элементов
     *
     ===========================================================================*/

    private void createPanels()
    {
        controlPanel = (JPanel) createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        add(createHistoryPanel());
        add(createSendPanel(), BorderLayout.SOUTH);
    }

    private Component createControlPanel()
    {
        JPanel pan = new JPanel(new GridLayout(2, 3, 2, 2));
        inpIP = new JTextField();
        inpPort = new JTextField();
        inpName = new JTextField();
        inpPassword = new JPasswordField();
        btnLogin = new JButton("Login");
        pan.add(inpIP);
        pan.add(inpPort);
        pan.add(btnLogin);
        pan.add(inpName);
        pan.add(inpPassword);
        return pan;
    }

    private Component createHistoryPanel()
    {
        historyPane = new JTextArea();
        historyPane.setEditable(false);
        return new JScrollPane(historyPane);
    }

    private Component createSendPanel()
    {
        JPanel pan = new JPanel(new BorderLayout(3, 3));
        inpMessage = new JTextField();
        btnSend = new JButton("Send");
        pan.add(inpMessage);
        pan.add(btnSend, BorderLayout.EAST);
        return pan;
    }


    /*===========================================================================
     *
     * Реализация рассылки сообщений зарегистрированным слушателям
     *
     ===========================================================================*/

    private void fireLogin(LoginEvent event)
    {
        LoginListener[] listeners = listenerList.getListeners(LoginListener.class);
        for (int i = listeners.length-1; i >= 0; i--)
            (listeners[i]).actionPerformed(event);
    }

    private void fireSendMessage(SendMessageEvent event)
    {
        SendMessageListener[] listeners = listenerList.getListeners(SendMessageListener.class);
        for (int i = listeners.length-1; i >= 0; i--)
            (listeners[i]).actionPerformed(event);
    }

    private void fireDisconnect(DisconnectEvent event)
    {
        DisconnectListener[] listeners = listenerList.getListeners(DisconnectListener.class);
        for (int i = listeners.length-1; i >= 0; i--)
            (listeners[i]).actionPerformed(event);
    }

}
