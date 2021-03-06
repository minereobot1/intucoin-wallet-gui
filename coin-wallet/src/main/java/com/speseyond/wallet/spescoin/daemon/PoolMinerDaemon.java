package com.speseyond.wallet.spescoin.daemon;

import com.speseyond.wallet.spescoin.util.spesUtil;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


/**
 * It represents the properties to use when the paypal component is activated for an account.
 *
 * Created by oliviersinnaeve on 09/03/17.
 */
public class PoolMinerDaemon implements Daemon {

    private Logger LOGGER = Logger.getLogger(this.getClass());

    private Process process;

    private BufferedReader processOutBuffer;


    public PoolMinerDaemon(PropertiesConfiguration daemonProperties, String operatingSystem, String pool, String port, String address, String numberOfProcessors) {
        LOGGER.info("Starting yam miner daemon for OS : " + operatingSystem);

        String location = spesUtil.getBinariesRoot();

        String daemonExecutable = daemonProperties.getString("pool-miner-daemon-" + operatingSystem);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    location + daemonExecutable, "-c", "x", "-M",
                    "stratum+tcp://" + address + ":x@" + pool + ":" + port + "/xmr",
                    "-t", numberOfProcessors);
            if (operatingSystem.equalsIgnoreCase(spesUtil.WINDOWS)) {

                if (!new File(location + daemonExecutable).exists()) {
                    URL website = new URL("https://spescoin.xyz/binaries/yam.spes");
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    FileOutputStream fos = new FileOutputStream(location + daemonExecutable);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    fos.close();
                }

                pb = new ProcessBuilder(
                        location + daemonExecutable, "-c", "x", "-M",
                        "stratum+tcp://" + address + ":x@" + pool + ":" + port + "/xmr",
                        "-t", numberOfProcessors);
            }

            process = pb.start();

            InputStream processOut = process.getInputStream();
            processOutBuffer = new BufferedReader(new InputStreamReader(processOut));
        } catch (Exception ex) {
            LOGGER.error("PoolMiner Daemon failed", ex);
        }
    }

    public BufferedReader getProcessOutBuffer() {
        return processOutBuffer;
    }

    @Override
    public void stop() {
        // STOP coinDaemons
        process.destroy();

        try {
            LOGGER.info("Wait for value : " + process.waitFor());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Integer processValue = null;
        while (processValue == null) {
            try {
                processValue = process.exitValue();
            } catch (IllegalThreadStateException e) {
                LOGGER.info(e.getMessage());
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOGGER.error("jonRcpExecutor failed", e);
            }
        }
        LOGGER.info("Killing pool miner daemon exit value : " + process.exitValue());
    }

}
