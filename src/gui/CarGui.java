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

import agents.CarAgent;

public class CarGui extends JFrame implements ActionListener{

    private static final long serialVersionUID = 434802339658676247L;
    
    final static int IN_PROCESS = 0;
    final static int WAIT_CONFIRM = 1;
    final static int IN_LINE = 2;
    final static int EXIT_SIGNAL = 0;
    final static int UPDATE_SIGNAL = 65;
    final static int STORE_SIGNAL = 55;
    final static int ALT_SIGNAL = 70;
    
    private int status = IN_PROCESS;
    private JTextField msg, timeTillUse, timeNeeded;
    private JLabel slotPos;
    private JButton store, update, alt, cancel, quit;

    private CarAgent myAgent;

    public CarGui(CarAgent car, Integer slotValue) {

        myAgent = car;

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
        p.add(slotPos = new JLabel("Slot position " + slotValue), BorderLayout.NORTH);

        panel.add(p, BorderLayout.CENTER);

        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout(0, 0));
        p1.add(new JLabel("Time Needed"), BorderLayout.NORTH);
        p1.add(timeNeeded = new JTextField(8));

        panel.add(p1, BorderLayout.SOUTH);
        panel = new JPanel();

        base.add(panel, BorderLayout.EAST);

        panel.setLayout(new BorderLayout(0, 10));
        pane = new JPanel();
        pane.setLayout(new BorderLayout(0, 0));
        pane.add(new JLabel("Time Till Use"), BorderLayout.NORTH);
        pane.add(timeTillUse = new JTextField(8));
        panel.add(pane, BorderLayout.SOUTH);

        pane = new JPanel();
        panel.add(pane, BorderLayout.NORTH);
        pane.setBorder(new EmptyBorder(0, 0, 130, 0));
        pane.setLayout(new GridLayout(5, 1, 0, 5));
        pane.add(store = new JButton("Store"));
        store.setToolTipText("Submit operation");
        store.addActionListener(this);
        pane.add(update = new JButton("Update"));
        update.setToolTipText("Submit operation");
        update.addActionListener(this);
        pane.add(alt = new JButton("Toggle Algorithm"));
        alt.setToolTipText("Submit operation");
        alt.addActionListener(this);
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
        else if (ae.getSource() == store) {
            if (timeTillUse.getText().length() == 0) {
                alertInfo("Enter an amount");
                timeTillUse.requestFocus();
                return;
            }
            else if (timeNeeded.getText().length() == 0) {
                alertInfo("Enter an amount");
                timeNeeded.requestFocus();
                return;
            }
            else if (timeTillUse.getText().matches("[0-9]+")
                    && timeNeeded.getText().matches("[0-9]+")) {
                GuiEvent ge = new GuiEvent(this, STORE_SIGNAL);
                ge.addParameter(new Integer(timeNeeded.getText()));
                ge.addParameter(new Integer(timeTillUse.getText()));
                myAgent.postGuiEvent(ge);
            }
            else {
                alertInfo("Enter numbers only");
                return;
            }
        }
        else if (ae.getSource() == update) {
            GuiEvent ge = new GuiEvent(this, UPDATE_SIGNAL); 
            myAgent.postGuiEvent(ge);   
         }
        else if (ae.getSource() == cancel && status != IN_LINE) {
            status = IN_PROCESS;
            cancel.setEnabled(false);
            msg.setText("Operation canceled!");
         }
        else if (ae.getSource() == alt) {
            GuiEvent ge = new GuiEvent(this, ALT_SIGNAL); 
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
            GuiEvent ge = new GuiEvent(this, EXIT_SIGNAL);
            myAgent.postGuiEvent(ge);
        }
    }

    public void alertResponse(String s) {
        slotPos.setText("Slot position " + s.toString());
    }

    public void resetStatus() {

        status = IN_PROCESS;
    }
}
