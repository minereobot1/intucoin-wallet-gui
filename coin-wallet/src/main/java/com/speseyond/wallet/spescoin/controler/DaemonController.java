package com.speseyond.wallet.spescoin.controler;

import com.speseyond.wallet.spescoin.controler.panel.NewWalletPanel;
import com.speseyond.wallet.spescoin.controler.panel.PasswordPanel;
import com.speseyond.wallet.spescoin.daemon.CoinDaemon;
import com.speseyond.wallet.spescoin.daemon.Daemon;
import com.speseyond.wallet.spescoin.daemon.WalletDaemon;
import com.speseyond.wallet.spescoin.util.spesUtil;
import com.speseyond.wallet.rpc.RpcPoller;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class DaemonController {


    private Logger LOGGER = Logger.getLogger(this.getClass());

    private Daemon coinDaemon;
    private WalletDaemon walletDaemon;

    private PropertiesConfiguration applicationProperties;
    private PropertiesConfiguration walletProperties;

    private String operatingSystem;

    private String userHome;
    private String password;
    private String container;

    private boolean firstStartup;


    public DaemonController(PropertiesConfiguration applicationProperties, PropertiesConfiguration walletProperties, String operatingSystem) {
        LOGGER.info("Loading Daemon controller");
        this.applicationProperties = applicationProperties;
        this.walletProperties = walletProperties;
        this.operatingSystem = operatingSystem;
        firstStartup = false;
        URL splashScreenLocation = Thread.currentThread().getContextClassLoader().getResource("splash.png");

        // start the daemon already !!
        // We use remote dameon now ...
        coinDaemon = new CoinDaemon(applicationProperties, operatingSystem);

        String configLocation = spesUtil.getConfigRoot();
        userHome = spesUtil.getUserHome();
        container = walletProperties.getString("container-file");

        File userHomeFiles = new File(userHome);
        userHomeFiles.mkdirs();

        if (new File(configLocation + "coin-wallet.conf").exists()) {
            try {
                walletProperties.load(new FileInputStream(configLocation + "coin-wallet.conf"));
                container = walletProperties.getString("container-file");

                if (!StringUtils.isBlank(container) && new File(userHome + container).exists()) {
                    PasswordPanel passwordPanel = new PasswordPanel(container);

                    JOptionPane.showMessageDialog(null, passwordPanel, "Enter wallet password",
                            JOptionPane.INFORMATION_MESSAGE, spesUtil.getIcon());

                    password = passwordPanel.getPasswordField().getText();
                }
            } catch (ConfigurationException | IOException e) {
                LOGGER.info("No wallet has been loaded ever");
            }
        }

        if (StringUtils.isBlank(container) || !new File(userHome + container).exists()) {

            // TODO: rethink this, This is not the correct place, else we would not need to include the wallet panel in the
            // backend part ...
            final NewWalletPanel newWalletPallet = new NewWalletPanel();

            boolean valid = false;
            while (!valid) {
                JOptionPane.showMessageDialog(null, newWalletPallet, "Create or Import",
                        JOptionPane.INFORMATION_MESSAGE, new ImageIcon(splashScreenLocation));

                newWalletPallet.getWalletNameField().setEnabled(true);
                valid = StringUtils.isNotBlank(newWalletPallet.getWalletNameField().getText()) && StringUtils.isNotBlank(newWalletPallet.getPasswordField().getText());
            }

            container = newWalletPallet.getWalletNameField().getText();
            password = newWalletPallet.getPasswordField().getText();

            firstStartup = true;
        }
    }

    public void restartDaemon() {
        coinDaemon.stop();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        coinDaemon = new CoinDaemon(applicationProperties, operatingSystem);
    }

    public void stopDaemon() {
        if (coinDaemon != null) {
            coinDaemon.stop();

            int daemonPort = getDaemonPort();
            int daemonRpcPort = walletProperties.getInt("rpc-bind-port");

            LOGGER.info("Checking ports : '" + daemonPort + "' : '" + daemonRpcPort + "'");

            while (!spesUtil.availableForConnection(daemonPort)
                    || !spesUtil.availableForConnection(daemonRpcPort)) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            coinDaemon = null;
        }
    }

//    public void startDaemon() {
//        int daemonPort = getDaemonPort();
//        int daemonRpcPort = walletProperties.getInt("rpc-bind-port");
//
//        if (spesUtil.availableForConnection(daemonPort)
//                && spesUtil.availableForConnection(daemonRpcPort)) {
//            coinDaemon = new CoinDaemon(applicationProperties, operatingSystem);
//        }
//    }

    public void startWallet() {
        int walletRpc = getWalletRpcPort();

        if (spesUtil.availableForConnection(walletRpc) || walletDaemon == null) {
            walletDaemon = new WalletDaemon(applicationProperties, operatingSystem, walletProperties, container, password, firstStartup);
        }
    }

    public void stop() {
        walletDaemon.stop();
        //coinDaemon.stop();

        String timestamp = new SimpleDateFormat("dd-MM-yyyy-hh-mm").format(new Date());
        LOGGER.info("Backing up for container : " + container + " : " + timestamp);
        File userHomeBackupFiles = new File(userHome + "/backup/" + timestamp);
        userHomeBackupFiles.mkdirs();
        try {
            walletProperties.save(new FileOutputStream(userHomeBackupFiles + spesUtil.SEPARATOR + "coin-wallet-" + timestamp + ".conf"));
        } catch (ConfigurationException | FileNotFoundException e) {
            e.printStackTrace();
        }

        spesUtil.backupWallet(userHomeBackupFiles + spesUtil.SEPARATOR, container, timestamp);
    }

    private int getDaemonPort() {
        return walletProperties.getInt("p2p-bind-port");
    }

    private int getWalletRpcPort() {
        return walletProperties.getInt("bind-port");
    }

    public boolean isWalletStarted() {
        return walletDaemon.isStarted();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOGGER.info("STOPPING DAEMON !!!!!!");
    }

}
