package mr.demonid.view;

import mr.demonid.commons.Account;
import mr.demonid.commons.ConnectStatus;
import mr.demonid.commons.Message;
import mr.demonid.view.listeners.ClientEventListener;


public interface View {

    /**
     * Вывод в поле истории сообщения
     * Контролирует, чтобы выполнение происходило в EDT.
     */
    void showMessage(String message);
    void errorMessage(String message);
    void innerMessage(Message message);

    /**
     * Установка статуса соединения (чтобы GUI это отобразило для пользователя).
     * Контролирует, чтобы выполнение происходило в EDT.
     */
    void setConnectStatus(ConnectStatus status);

    /**
     * Установка и чтение данных о пользователе
     * Контролирует, чтобы выполнение происходило в EDT.
     */
    void setAccount(Account account);
    Account getAccount();

    /**
     * Установка слушателей для контролов ClientView
     */
    <T extends ClientEventListener> void addListener(Class<T> t, T l);

    /**
     * Удаление слушателей для контролов ClientView
     */
    <T extends ClientEventListener> void removeListeners(Class<T> t, T l);


}
