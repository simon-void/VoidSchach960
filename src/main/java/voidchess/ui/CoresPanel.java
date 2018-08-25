/*
 * Created on 01.11.2006
 */

package voidchess.ui;

import voidchess.player.ki.ComputerPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CoresPanel extends JPanel implements ActionListener {

    /**
     * @return true if more than one core is available
     */
    public boolean hasOptions() {
        return comboBox.getItemCount() > 1;
    }

    final private ComputerPlayer player;

    final private JComboBox<String> comboBox;

    public CoresPanel(ComputerPlayer player) {
        this.player = player;

        comboBox = new JComboBox<>();
        designLayout();
        //preselect the option of the maximum number of cores
        comboBox.setSelectedIndex(comboBox.getItemCount() - 1);
    }

    private void designLayout() {
        setBackground(Color.WHITE);
//        setBorder(new LineBorder(Color.LIGHT_GRAY));

        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        if (numberOfCores == 1) {
            comboBox.addItem("1");
        } else {
            comboBox.addItem(Integer.toString(numberOfCores - 1));
            comboBox.addItem(Integer.toString(numberOfCores));
        }
        comboBox.setEditable(false);
        comboBox.addActionListener(this);

//        add(new JLabel("#cores:"));
        add(comboBox);
    }

    public void setEnabled(boolean enable) {
        comboBox.setEnabled(enable);
    }

    public void actionPerformed(ActionEvent event) {
        int numberOfCoresToUse = 1;
//        try{
        numberOfCoresToUse = Integer.parseInt(comboBox.getSelectedItem().toString());
//        }catch(NumberFormatException e){}
        player.setNumberOfCoresToUse(numberOfCoresToUse);
    }

    public JPanel getLabel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("#cores:");
        panel.add(label);
        return panel;
    }
}
