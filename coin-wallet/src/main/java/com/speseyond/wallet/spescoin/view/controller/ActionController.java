package com.speseyond.wallet.spescoin.view.controller;

import com.speseyond.wallet.spescoin.controler.CoinRpcController;
import com.speseyond.wallet.spescoin.controler.DaemonController;
import com.speseyond.wallet.spescoin.controler.WalletRpcController;
import com.speseyond.wallet.spescoin.daemon.WalletDaemon;
import com.speseyond.wallet.spescoin.rpc.TransactionItemsRpcPoller;
import com.speseyond.wallet.spescoin.rpc.UnconfirmedTransactionHashesRpcPoller;
import com.speseyond.wallet.rpc.JsonRpcExecutor;
import com.speseyond.wallet.rpc.NoParamsRpcPoller;
import com.speseyond.wallet.rpc.RpcPoller;
import com.speseyond.wallet.rpc.model.Address;
import com.speseyond.wallet.rpc.model.AddressBalance;
import com.speseyond.wallet.rpc.model.AddressInput;
import com.speseyond.wallet.rpc.model.SpendKeys;
import com.speseyond.wallet.rpc.model.Success;
import com.speseyond.wallet.rpc.model.coin.BlockWrapper;
import com.speseyond.wallet.rpc.exception.KnownJsonRpcException;
import com.speseyond.wallet.spescoin.util.spesUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;


public class ActionController {

    private Logger LOGGER = Logger.getLogger(this.getClass());

    private DaemonController controller;
    private CoinRpcController coinRpcController;
    private WalletRpcController walletRpcController;
    private PoolMiningController miningController;
    private SoloMiningController soloMiningController;

    private List<RpcPoller> walletRpcPollers;


    public ActionController(final DaemonController controller, WalletRpcController walletRpcController, CoinRpcController coinRpcController) {
        this.controller = controller;
        this.coinRpcController = coinRpcController;
        this.walletRpcController = walletRpcController;
    }

    public void setMiningController(PoolMiningController miningController) {
        this.miningController = miningController;
    }

    public void setSoloMiningController(SoloMiningController soloMiningController) {
        this.soloMiningController = soloMiningController;
    }

    public void stopBackgroundProcessesBeforeReset() {
        coinRpcController.stop();
        walletRpcController.stop();
    }

    public void startBackgroundProcessesAfterReset() {
        coinRpcController.restart();
        walletRpcController.restart();
    }

    public Address createAddress() {
        try {
            return walletRpcController.getCreateAddressExecutor().execute(JsonRpcExecutor.EMPTY_PARAMS);
        } catch (KnownJsonRpcException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Address importAddress(AddressInput input) {
        try {
            return walletRpcController.getCreateAddressExecutor().execute(input.getParams());
        } catch (KnownJsonRpcException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BlockWrapper getBlockWrapper(String hash) {
        try {
            return coinRpcController.getBlockWrapperExecutor().execute("\"params\": {\"hash\": \"" + hash + "\"}");
        } catch (KnownJsonRpcException e) {
            restartCoinDaemon();
        }
        return null;
    }

    public void exit() {
        LOGGER.info("ActionController.exit was called");
        // Save the wallet
        try {
            walletRpcController.getSaveExecutor().execute(JsonRpcExecutor.EMPTY_PARAMS);
        } catch (KnownJsonRpcException e) {
            e.printStackTrace();
        }
        LOGGER.info("ActionController.exit was called");
        miningController.stopMining();
        soloMiningController.stopMining();
        coinRpcController.stop();
        walletRpcController.stop();
        controller.stop();
    }

    public void restartCoinDaemon() {
        controller.restartDaemon();
    }

//    public void startCoinDaemon() {
//        controller.startDaemon();
//    }

    public void stopCoinDaemon() {
        controller.stopDaemon();
    }

    public CoinRpcController getCoinRpcController() {
        return coinRpcController;
    }

    public WalletRpcController getWalletRpcController() {
        return walletRpcController;
    }

    public void startWallet(List<RpcPoller> walletRpcPollers) {
        controller.startWallet();

        while (!controller.isWalletStarted()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Add pollers if wallet rpc port is available
        for (RpcPoller poller : walletRpcPollers) {
            getCoinRpcController().addPollers(poller);
        }
    }

    public void resetWallet() {
        try {
            //controller.stopDaemon();
            walletRpcController.getResetExecutor().execute(JsonRpcExecutor.EMPTY_PARAMS);

            for (RpcPoller poller : walletRpcPollers) {
                if (poller instanceof TransactionItemsRpcPoller) {
                    ((TransactionItemsRpcPoller) poller).reset();
                }
            }

            //controller.startDaemon();
        } catch (KnownJsonRpcException e) {
            e.printStackTrace();
        }
    }

    public void resetBlockChain() {
        LOGGER.info("Delete block chain");
        LOGGER.debug("Command : " + spesUtil.getDeleteBlockChainHomeCommand());

        stopCoinDaemon();

//        ProcessBuilder pb = new ProcessBuilder(spesUtil.getDeleteBlockChainHomeCommand());

        Process process;
        try {
//            process = pb.start();
            process = Runtime.getRuntime().exec(spesUtil.getDeleteBlockChainHomeCommand());
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            LOGGER.info("Windows sleep : 5 seconds");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopCoinDaemon();
    }

    public SpendKeys getSpendKeys(String address) {
        try {
            return walletRpcController.getSpendKeysExecutor().execute("\"params\": {\"address\": \"" + address + "\"}");
        } catch (KnownJsonRpcException e) {
            e.printStackTrace();
        }


        return null;
    }

    public AddressBalance getBalance(String address) {
        LOGGER.info("Get address balance : " + address);

        try {
            return this.walletRpcController.getBalanceExecutor().execute("\"params\": {" +
                    "\"address\": \"" + address + "\"" +
                    "}");
        } catch (KnownJsonRpcException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Success deleteAddress(String address) {
        try {
            return this.walletRpcController.getDeleteAddressExecutor().execute("\"params\": {" +
                "\"address\": \"" + address + "\"" +
            "}");
        } catch (KnownJsonRpcException e) {
            e.printStackTrace();
            return null;
        }
    }

}
