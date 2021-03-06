package com.speseyond.wallet.spescoin.view.view;

import com.speseyond.wallet.rpc.exception.KnownJsonRpcException;
import com.speseyond.wallet.rpc.model.Addresses;
import com.speseyond.wallet.rpc.model.Payment;
import com.speseyond.wallet.rpc.model.PaymentInput;
import com.speseyond.wallet.spescoin.util.CoinUtil;
import com.speseyond.wallet.spescoin.util.ComponentFactory;
import com.speseyond.wallet.spescoin.view.controller.PaymentController;
import com.speseyond.wallet.spescoin.view.model.JComboboxItem;
import com.speseyond.wallet.spescoin.view.view.panel.AbstractAddressJPanel;
import com.speseyond.wallet.spescoin.view.view.panel.TransferPanel;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;


public class CreatePaymentTabView extends AbstractAddressJPanel implements ActionListener, Observer {

    private Logger LOGGER = Logger.getLogger(this.getClass());

    private PaymentController paymentController;

    private JPanel transferPanel;
    private List<TransferPanel> transfers = new ArrayList<>();

    private JTextField paymentId;
    private JTextField fee;
    private JComboBox<JComboboxItem> mixinCount;


    public CreatePaymentTabView(PaymentController controller) {

        this.paymentController = controller;
        //construct preComponents
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[]  { 1, 1, 1, 1, 1 };
        gbl.rowHeights = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        gbl.columnWeights = new double[]{0.01, 0.48, 0.02, 0.48, 0.01};
        gbl.rowWeights = new double[] { 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.80, 0.02, 0.02, 0.02 };
        setLayout(gbl);

        //construct components
        JLabel paymentIDLabel = new JLabel("Payment id :");
        JLabel mixinCountLabel = new JLabel("Mixin count / anonymity :");
        JLabel addressFromLabel = new JLabel ("Address from :");
        JLabel feeLabel = new JLabel ("Fee :");

        addresses = new JComboBox<>();
        mixinCount = new JComboBox<>();
        paymentId = new JTextField();

        for (int i = 0; i <= 10; i++) {
            JComboboxItem item = new JComboboxItem(i, i);
            mixinCount.addItem(item);
        }

        NumberFormat amountFormat = NumberFormat.getNumberInstance();
        amountFormat.setGroupingUsed(false);
        amountFormat.setMinimumFractionDigits(10);
        amountFormat.setMaximumFractionDigits(10);
        fee = new JFormattedTextField(amountFormat);
        //fee.setColumns(35);
        fee.setText("0.0001000000");

        JButton addPaymentButton = ComponentFactory.createSubButton("Add payment");
        JButton createPaymentButton = ComponentFactory.createSubButton("Create payment(s)");

        //set components properties
        addressFromLabel.setToolTipText ("Address from");
        createPaymentButton.setToolTipText ("Create payment(s)");

        addPaymentButton.addActionListener(this);
        createPaymentButton.addActionListener(this);

        TransferPanel panel = new TransferPanel(true, this);
        transfers.add(panel);
        GridLayout layout = new GridLayout(0 ,1);
        transferPanel = new JPanel(layout);
        layout.setVgap(10);
        transferPanel.add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(paymentIDLabel, gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        add(paymentId, gbc);


        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(addressFromLabel, gbc);

        gbc.gridx = 3;
        gbc.gridy = 3;
        add(addresses, gbc);

        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 1;
        gbc.gridy = 5;
        add(feeLabel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 3;
        gbc.gridy = 5;
        add(fee, gbc);

        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 1;
        gbc.gridy = 7;
        add(mixinCountLabel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 3;
        gbc.gridy = 7;
        add(mixinCount, gbc);

        gbc.gridwidth = 3;
        gbc.gridx = 1;
        gbc.gridy = 9;
        add(transferPanel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 1;
        gbc.gridy = 12;
        add(addPaymentButton, gbc);

        gbc.gridx = 3;
        gbc.gridy = 12;
        add(createPaymentButton, gbc);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof Addresses) {
            if (!data.equals(addressesList)) {
                addressesList = (Addresses) data;
                update(addressesList);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Create payment(s)")) {
            LOGGER.info("Payment button was clicked ...");
            PaymentInput input = new PaymentInput();
            input.setPaymentId(paymentId.getText());
            input.setAddress(((JComboboxItem)addresses.getSelectedItem()).getValue());
            input.setAnonymity(Integer.parseInt(((JComboboxItem)mixinCount.getSelectedItem()).getValue()));
            long feeAmount = CoinUtil.getLongForText(fee.getText());
            input.setFee(feeAmount);
            Map<String, Long> transferList = new HashMap<>();

            LOGGER.info("Creating payment with address : " + input.getAddress());

            for (TransferPanel tmpTransfer : transfers) {
                if (tmpTransfer != null) {
                    LOGGER.info("Adding destination address : " + tmpTransfer.getAddress().getText());
                    LOGGER.info("Converting amount text to long : " + tmpTransfer.getAmount().getText());
                    long amount = CoinUtil.getLongForText(tmpTransfer.getAmount().getText());
                    LOGGER.info("Adding amount : " + amount);

                    if (transferList.get(tmpTransfer.getAddress().getText()) != null) {
                        amount += transferList.get(tmpTransfer.getAddress().getText());
                    }

                    transferList.put(tmpTransfer.getAddress().getText(), amount);
                }
            }
            input.setTransfers(transferList);

            LOGGER.info("Start to create payment ...");

            int result = JOptionPane.showConfirmDialog(this,
                    "You sure you want to execute the payment ?",
                    "Create payment ?",
                    JOptionPane.YES_NO_OPTION);

            if (result == 0) {
                Payment payment;
                try {
                    payment = paymentController.makePayment(input);

                    if (payment != null) {
                        for (TransferPanel tmpTransfer : transfers) {
                            transferPanel.remove(tmpTransfer);
                        }

                        transfers = new ArrayList<>();

                        TransferPanel newPanel = new TransferPanel(false, this);
                        transfers.add(newPanel);
                        transferPanel.add(newPanel);
                    }
                } catch (KnownJsonRpcException e1) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to execute payment : " + e1.getError().getMessage(),
                            "Fatal error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        if (command.equals("Add payment")) {
            TransferPanel newPanel = new TransferPanel(false, this);
            transfers.add(newPanel);
            transferPanel.add(newPanel);
            newPanel.setActionListeners();

            repaint();
            updateUI();
        }
    }

    public void removePanel(TransferPanel panel) {
        transfers.remove(panel);
    }
}
