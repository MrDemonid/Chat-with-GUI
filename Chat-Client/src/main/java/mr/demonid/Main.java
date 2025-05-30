package mr.demonid;


import mr.demonid.controller.Client;
import mr.demonid.view.ViewSwing;

import javax.swing.*;


public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client(new ViewSwing());
            }
        });

    }
}