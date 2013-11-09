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

import agents.Initialiser;

public class InitialiserGui extends JFrame implements ActionListener {

    //initialise variables
    private static final long serialVersionUID = 3336627106461024376L;
    
    final static int EXIT_SIGNAL = 0;
	final static int ADD_SIGNAL = 69;
	
	private JTextField msg;
	private JButton add, quit;
	private JLabel carAmount;

	private Initialiser myAgent;
	Integer amount = new Integer(0);

    //create the initial gui look
	public InitialiserGui(Initialiser init, Integer amount) {

		myAgent = init;
		this.amount = amount;

		setTitle(myAgent.getLocalName());

        JPanel base = new JPanel();
        base.setBorder(new EmptyBorder(15, 15, 15, 15));
        base.setLayout(new BorderLayout(10, 10));
        getContentPane().add(base);

        JPanel panel = new JPanel();
        base.add(panel, BorderLayout.WEST);
        panel.setLayout(new BorderLayout(0, 2));
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(0, 0));
        pane.add(new JLabel("Message"), BorderLayout.NORTH);

        pane.add(msg = new JTextField("No messages recieved", 15));
        msg.setEditable(false);
        msg.setHorizontalAlignment(JTextField.CENTER);
        panel.add(pane, BorderLayout.NORTH);

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 1));
        p.add(carAmount = new JLabel("Number of cars: " + this.amount));
        panel.add(p, BorderLayout.WEST);


        pane = new JPanel();
        panel.add(pane, BorderLayout.EAST);
        pane.setBorder(new EmptyBorder(0, 20, 130, 0));
        pane.setLayout(new GridLayout(2, 1, 0, 5));
        pane.add(add = new JButton("Add Cars"));
        add.setToolTipText("Added Cars");
        add.addActionListener(this);
        pane.add(quit = new JButton("QUIT"));
        quit.setToolTipText("Stop agent and exit");
        quit.addActionListener(this);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				shutDown();
			}
		});

        setSize(285, 275);
		setResizable(false);

	}

    //provide functionality to gui events
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == quit) {
			shutDown();
		} else if (ae.getSource() == add) {
			alertInfo("Add Car agent");
			GuiEvent ge = new GuiEvent(this, ADD_SIGNAL);
			myAgent.postGuiEvent(ge);
		} 
	}

    //updates message string
	void alertInfo(String s) {
		// --------------------------

		Toolkit.getDefaultToolkit().beep();
		msg.setText(s);
	}

    public void alertResponse(String response) {
        msg.setText(response);        
    }
    
    public void alertCount(String response) {
        carAmount.setText("Number of cars: " + response);  
        this.amount = Integer.parseInt(response);
    }

    //shutdown gui
	void shutDown() {
		int rep = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to exit?", myAgent.getLocalName(),
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (rep == JOptionPane.YES_OPTION) {
			GuiEvent ge = new GuiEvent(this, EXIT_SIGNAL);
			myAgent.postGuiEvent(ge);
		}
	}
}
