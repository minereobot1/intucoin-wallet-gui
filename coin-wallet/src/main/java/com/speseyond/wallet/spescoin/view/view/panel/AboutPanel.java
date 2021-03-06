package com.speseyond.wallet.spescoin.view.view.panel;

import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;


public class AboutPanel extends JPanel {

    private Logger LOGGER = Logger.getLogger(this.getClass());

    /**
     * Create the panel.
     */
    public AboutPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(600, 250));

        //Create an array of the text and components to be displayed.
        String msgString1 =
                "<html><body>" +
                        "<h1>SpesCoin wallet</h1>"
                        + "<br>"
                        + "SpesCoin wallet brougth to you by @asm and SpesCoin"
                        + "<br><br>"
                        + "Credits to oliviersinnaeve"
                        + "<br><br>"
                        + "Want to help? Get involved!"
                        + "<br><br>"
                        + "</body></html>";

        JPanel footer = new JPanel();

        String websiteLink = "<html><body><br><a href='https://myspes.org'>Website</a> &nbsp;&nbsp;|&nbsp;&nbsp;</body></html>";
        JLabel websiteLinkLabel = new JLabel(websiteLink);
        makeLinkable(websiteLinkLabel, new LinkMouseListener());

        String walletLink = "<html><body><br><a href='https://discord.gg/TxJtH5F'>Discord Channel</a> &nbsp;&nbsp;|&nbsp;&nbsp;</body></html>";
        JLabel walletLinkLabel = new JLabel(walletLink);
        makeLinkable(walletLinkLabel, new LinkMouseListener());

        String poolLink = "<html><body><br><a href='http://pool.myspes.org'>SpesCoin Pool</a> &nbsp;&nbsp;|&nbsp;&nbsp;</body></html>";
        JLabel poolLinkLabel = new JLabel(poolLink);
        makeLinkable(poolLinkLabel, new LinkMouseListener());

        String twitterLink = "<html><body><br><a href='https://twitter.com/SpesCoin'>Twitter</a> &nbsp;&nbsp;|&nbsp;&nbsp;</body></html>";
        JLabel twitterLinkLabel = new JLabel(twitterLink);
        makeLinkable(twitterLinkLabel, new LinkMouseListener());

        String facebookLink = "<html><body><br><a href='https://www.facebook.com/spes.coin'>Facebook</a> &nbsp;&nbsp;|&nbsp;&nbsp;</body></html>";
        JLabel facebookLinkLabel = new JLabel(facebookLink);
        makeLinkable(facebookLinkLabel, new LinkMouseListener());

        String bitcoinTalkLink = "<html><body><br><a href='https://bitcointalk.org/index.php?topic=2870436.0'>Bitcointalk</a></body></html>";
        JLabel bitcoinTalkLinkLabel = new JLabel(bitcoinTalkLink);
        makeLinkable(bitcoinTalkLinkLabel, new LinkMouseListener());

        footer.add(websiteLinkLabel);
        footer.add(walletLinkLabel);
        footer.add(poolLinkLabel);
        footer.add(twitterLinkLabel);
        footer.add(facebookLinkLabel);
        footer.add(bitcoinTalkLinkLabel);
        add(footer, BorderLayout.SOUTH);

        //add(new DonationPanel(), BorderLayout.CENTER);

        JLabel helpMessageLabel = new JLabel(msgString1);
        add(helpMessageLabel, BorderLayout.NORTH);
    }


    private void makeLinkable(JLabel c, MouseListener ml) {
        assert ml != null;
        c.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        c.addMouseListener(ml);
    }

    private static class LinkMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            JLabel l = (JLabel) evt.getSource();
            try {
                URI uri = new java.net.URI(AboutPanel.getPlainLink(l.getText()));
                (new LinkRunner(uri)).execute();
            } catch (URISyntaxException use) {
                throw new AssertionError(use + ": " + l.getText()); //NOI18N
            }
        }
    }

    private static class LinkRunner extends SwingWorker<Void, Void> {

        private final URI uri;

        private LinkRunner(URI u) {
            if (u == null) {
                throw new NullPointerException();
            }
            uri = u;
        }

        @Override
        protected Void doInBackground() throws Exception {
            Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.browse(uri);
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
            } catch (ExecutionException ee) {
                handleException();
            } catch (InterruptedException ie) {
                handleException();
            }
        }

        private static void handleException() {
            JOptionPane.showMessageDialog(null, "Sorry, a problem occurred while trying to open this link in your system's standard browser.", "A problem occured", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String getPlainLink(String s) {
        final String A_HREF = "<a href='";
        final String HREF_CLOSED = "'>";
        return s.substring(s.indexOf(A_HREF) + A_HREF.length(), s.indexOf(HREF_CLOSED));
    }

}
