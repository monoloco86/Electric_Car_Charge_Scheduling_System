
package gui;

import jade.gui.GuiEvent;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import agents.TransformerAgent;

public class TransformerGui extends JFrame implements ActionListener {

    private static final long serialVersionUID = 4202866304780272910L;

    final static int EXIT_SIGNAL = 0;
    final static int UPDATE_SIGNAL = 67;
    final static int RAND_SIGNAL = 72;
    private JTextField msg;
    private JLabel energyLimit, currentEnergy;
    private JButton update, rand, cancel, quit;
    private JTable slotOrder;

    private Map<String, Integer> map = new HashMap<String, Integer>();
    private TransformerAgent myAgent;

    public TransformerGui(TransformerAgent transformer, Integer limit) {

        myAgent = transformer;

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
        pane = new JPanel();
        pane.setLayout(new BorderLayout(5, 0));

        TableModel model = new TableDataModel(map);
        slotOrder = new JTable(model);
        pane.add(new JScrollPane(slotOrder), BorderLayout.WEST);
        pane.setSize(20, 30);
        panel.add(pane, BorderLayout.WEST);
        pane = new JPanel();
        pane.setLayout(new BorderLayout(5, 0));

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(energyLimit = new JLabel("Energy Limit: " + limit), BorderLayout.NORTH);
        p.add(currentEnergy = new JLabel("Current Energy: " + 0));

        panel.add(p, BorderLayout.SOUTH);
        panel = new JPanel();
        base.add(panel, BorderLayout.EAST);
        panel.setLayout(new BorderLayout(0, 10));

        pane = new JPanel();
        panel.add(pane, BorderLayout.EAST);
        pane.setBorder(new EmptyBorder(0, 0, 130, 0));
        pane.setLayout(new GridLayout(10, 1, 0, 5));
        pane.add(update = new JButton("Update"));
        update.setToolTipText("Submit operation");
        update.addActionListener(this);
        pane.add(rand = new JButton("Random Charge"));
        rand.setToolTipText("Submit operation");
        rand.addActionListener(this);
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

        setSize(650, 550);
        setResizable(false);

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == quit) {
            shutDown();
        }
        else if (ae.getSource() == update) {
            alertInfo("Update charge");
            GuiEvent ge = new GuiEvent(this, UPDATE_SIGNAL);
            myAgent.postGuiEvent(ge);
        }
        else if (ae.getSource() == rand) {
            alertInfo("Random charge");
            GuiEvent ge = new GuiEvent(this, RAND_SIGNAL);
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

    @SuppressWarnings("unchecked")
    public void alertResponse(Object o) {
        if (o instanceof String)
            msg.setText((String) o);
        else if (o instanceof Map) {
			System.out.println("Inside GUI");
			for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) o).entrySet()) {
				System.out.println("LOOPING");
				System.out.println(entry.getKey() + ": " + entry.getValue());
			}
            TableDataModel model = new TableDataModel((Map<String, Integer>) o);
            slotOrder.setModel(model);
            slotOrder.repaint();
        }
    }

    public void alertLimit(Integer i) {
        energyLimit.setText("Energy Limit: " + i.toString());
    }

    public void alertCurrent(Integer i) {
        currentEnergy.setText("Current Energy: " + i.toString());
    }

    private class TableDataModel extends AbstractTableModel {

        private static final long serialVersionUID = -9046466617697329165L;
        private Map<String, Integer> data;
        private String[] keys;

        public TableDataModel(Map<String, Integer> tmap) {
            data = tmap;
            keys = data.keySet().toArray(new String[data.size()]);
        }

        @Override
        public String getColumnName(int col) {
            if (col == 0) {
                return "Agent";
            } else {
                return "Value";
            }
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return keys[row];
            } else if(col == 1){
                return data.get(keys[row]);
            } else
                return null;
                
        }

    }
}
