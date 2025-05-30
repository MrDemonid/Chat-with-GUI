package mr.demonid.view;


import mr.demonid.view.listeners.ServerEventListener;

public interface View
{

    /**
     * Прием сообщения для отображения в информационной консоли
     * @param message Сообщение формата "[@]Username: message"
     */
    void showMessage(String message);

    /**
     * Установка и удаление слушателей для контролов View
     * Слушатели ограничены реализацией ServerEventListener и его потомков
     */
    <T extends ServerEventListener> void removeListeners(Class<T> t, T l);
    <T extends ServerEventListener> void addListener(Class<T> t, T l);

}
