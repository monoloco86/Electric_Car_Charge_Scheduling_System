
package gui;

import jade.gui.GuiEvent;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import agents.SummaryAgent;

public class SummaryGui extends JFrame implements ActionListener{

    private static final long serialVersionUID = -4294488323535790208L;

    final static int IN_PROCESS = 0;
    final static int WAIT_CONFIRM = 1;
    final static int IN_LINE = 2;
    int status = IN_PROCESS;
    private JTextField msg;
    private JLabel slotPos;
    private JButton update, cancel, quit;

    private SummaryAgent myAgent;

    public SummaryGui(SummaryAgent summary) {

        myAgent = summary;

        setTitle(myAgent.getLocalName());

        JPanel base = new JPanel();
        base.setBorder(new EmptyBorder(15, 15, 15, 15));
        base.setLayout(new BorderLayout(10, 10));
        getContentPane().add(base);

        JPanel panel = new JPanel();
        base.add(panel, BorderLayout.WEST);
        panel.setLayout(new BorderLayout(0, 5));
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(0, 0));
        pane.add(new JLabel("Message"), BorderLayout.NORTH);

        pane.add(msg = new JTextField("No messages recieved", 15));
        msg.setEditable(false);
        msg.setHorizontalAlignment(JTextField.CENTER);
        panel.add(pane, BorderLayout.NORTH);

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(slotPos = new JLabel("Slot positions"), BorderLayout.NORTH);

        panel.add(pane, BorderLayout.SOUTH);

        pane = new JPanel();
        panel.add(pane, BorderLayout.EAST);
        pane.setBorder(new EmptyBorder(100, 50, 100, 50));
        pane.setLayout(new GridLayout(1, 3));
        pane.add(update = new JButton("Update"));
        update.setToolTipText("Submit operation");
        update.addActionListener(this);
        pane.add(cancel = new JButton("Cancel"));
        cancel.setToolTipText("Submit operation");
        cancel.setEnabled(false);
        cancel.addActionListener(this);
        pane.add(quit = new JButton("QUIT"));
        quit.setToolTipText("Stop agent and exit");
        quit.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                shutDown();
            }
        });

        setSize(470, 350);
        setResizable(false);

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == quit) {
            shutDown();
        }
        else if (ae.getSource() == update) {
            alertInfo("Update slot list");
            GuiEvent ge = new GuiEvent(this, 55);
            myAgent.postGuiEvent(ge);
        }
    }

    void alertInfo(String s) {
        // --------------------------

        Toolkit.getDefaultToolkit().beep();
        msg.setText(s);
    }

    void shutDown() {
        // ----------------- Control the closing of this gui

        int rep = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
                myAgent.getLocalName(),
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (rep == JOptionPane.YES_OPTION) {
            GuiEvent ge = new GuiEvent(this, 0);
            myAgent.postGuiEvent(ge);
        }
    }

    public void alertResponse(String s) {
        slotPos.setText(s.toString());
    }

    public void resetStatus() {

        status = IN_PROCESS;
    }
}
