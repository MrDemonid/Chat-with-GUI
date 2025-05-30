package mr.demonid;


import mr.demonid.controller.Server;
import mr.demonid.view.ViewSwing;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Server server = new Server(new ViewSwing());
            }
        });
    }
}