package fox.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class FOptionPane extends JDialog implements ActionListener {
    public enum TYPE {DEFAULT, YES_NO_TYPE}
    private TYPE type;

    private JButton OK_BUTTON;
    private JButton NO_BUTTON, YES_BUTTON;
    private BufferedImage ico;
    private int answer = -1, timeout = 20;
    private static Thread toThread;
    private static JLabel toLabel;

    public FOptionPane(String title, String message, TYPE type, BufferedImage ico) {
        this.type = type == null ? TYPE.DEFAULT : type;
        if (ico != null) {
            this.ico = ico;
        } else {
            try {
                this.ico = ImageIO.read(new File("./resources/icons/favorite.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        setModal(true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);

        setTitle(title);
        setAlwaysOnTop(true);
        setUndecorated(true);
        setPreferredSize(new Dimension(300, 150));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getRootPane().setBorder(new EmptyBorder(3,3,3,3));

        JPanel basePane = new JPanel(new BorderLayout(3,3)) {
            {
                setBackground(Color.DARK_GRAY);
                setBorder(new EmptyBorder(3,3,3,3));

                JPanel icoPane = new JPanel() {
                    @Override
                    public void paintComponent(Graphics g) {
                        if (FOptionPane.this.ico != null) {
                            g.drawImage(FOptionPane.this.ico,

                                    0, 0,
                                    64, 64,

                                    0, 0,
                                    FOptionPane.this.ico.getWidth(), FOptionPane.this.ico.getHeight(),

                                    this);
                        }
                    };

                    {
                        setPreferredSize(new Dimension(64, 0));
                    }
                };

                toLabel = new JLabel() {
                    {
                        setForeground(Color.GRAY);
                    }
                };

                JPanel mesPane = new JPanel(new BorderLayout(3, 3)) {
                    {
                        setOpaque(false);
                        setBorder(new EmptyBorder(16,6,0,0));

                        JTextArea mesArea = new JTextArea() {
                            {
                                setEditable(false);
                                setForeground(Color.WHITE);
                                setText(message);
                                setWrapStyleWord(true);
                                setLineWrap(true);
                                setBorder(null);
                                setBackground(Color.DARK_GRAY);
                            }
                        };

                        JScrollPane mesScroll = new JScrollPane(mesArea) {
                            {
                                setOpaque(false);
                                getViewport().setOpaque(false);
                                setBorder(null);
                                getViewport().setBorder(null);
                            }
                        };

                        add(mesScroll);
                    }
                };

                JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3)) {
                    {
                        setOpaque(false);

                        switch (FOptionPane.this.type) {
                            case DEFAULT:
                                OK_BUTTON = new JButton("OK") {
                                    {
                                        setActionCommand("ok");
                                        addActionListener(FOptionPane.this);
                                    }
                                };
                                add(OK_BUTTON);
                                break;
                            case YES_NO_TYPE:
                                YES_BUTTON = new JButton("????") {
                                    {
                                        setActionCommand("yes");
                                        addActionListener(FOptionPane.this);
                                    }
                                };
                                NO_BUTTON = new JButton("??????") {
                                    {
                                        setActionCommand("no");
                                        addActionListener(FOptionPane.this);
                                    }
                                };
                                add(YES_BUTTON);
                                add(NO_BUTTON);
                                break;
                            default:

                        }
                    }
                };

                add(icoPane, BorderLayout.WEST);
                add(toLabel, BorderLayout.NORTH);
                add(mesPane, BorderLayout.CENTER);
                add(btnPane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        toThread = new Thread(() -> {
            while (timeout > 0) {
                timeout--;
                toLabel.setText("????????????????: " + timeout + " ??????.");
                try {Thread.sleep(1000);
                } catch (InterruptedException e) {e.printStackTrace();}
            }
            answer = 1;
            FOptionPane.this.dispose();
        });
        toThread.start();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public int get() {
        return answer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "yes": answer = 0;
                break;
            case "no": answer = -1;
                break;
            case "ok":
            default:
        }

        FOptionPane.this.dispose();
    }
}
