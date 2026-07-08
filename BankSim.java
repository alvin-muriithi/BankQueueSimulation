import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BankSim extends JFrame {

    // Input fields
    private JTextField txtNumCustomers;
    private JTextField txtArrivalMin;
    private JTextField txtArrivalMax;
    private JTextField txtServiceMin;
    private JTextField txtServiceMax;
    private JTextField txtSeed;
    private JTextField txtSimulationSpeed;

    // Output areas
    private JTextArea txtStatistics;
    private JButton btnRun;
    private JLabel lblServerStatus;
    private JPanel queuePanel;
    private JPanel serverPanel;
    private JLabel lblCurrentTime;
    private JLabel lblCustomersServed;
    private JLabel lblCurrentQueueLength;
    
    // Simulation state
    private boolean isRunning = false;
    private List<CustomerVisual> customersInQueue = new ArrayList<>();
    private CustomerVisual currentCustomer = null;
    private Timer animationTimer;

    public BankSim() {
        super("Bank Queue Simulation - Visual");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
    }

    private void initComponents() {
        // ========== TOP: Control Panel ==========
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "Simulation Parameters", 
            TitledBorder.CENTER, 
            TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 14),
            new Color(70, 130, 180)
        ));
        controlPanel.setBackground(new Color(240, 248, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        txtNumCustomers = createStyledTextField("100", 8);
        txtArrivalMin = createStyledTextField("0.5", 8);
        txtArrivalMax = createStyledTextField("3.0", 8);
        txtServiceMin = createStyledTextField("1.0", 8);
        txtServiceMax = createStyledTextField("4.0", 8);
        txtSeed = createStyledTextField("12345", 8);
        txtSimulationSpeed = createStyledTextField("100", 8);

        addControlRow(controlPanel, gbc, 0, "Number of Customers:", txtNumCustomers);
        addControlRow(controlPanel, gbc, 1, "Arrival Time (min):", txtArrivalMin, txtArrivalMax);
        addControlRow(controlPanel, gbc, 2, "Service Time (min):", txtServiceMin, txtServiceMax);
        addControlRow(controlPanel, gbc, 3, "Random Seed:", txtSeed);
        addControlRow(controlPanel, gbc, 4, "Animation Speed (ms):", txtSimulationSpeed);

        btnRun = new JButton("▶ Run Simulation");
        btnRun.setFont(new Font("Arial", Font.BOLD, 14));
        btnRun.setBackground(new Color(70, 130, 180));
        btnRun.setForeground(Color.WHITE);
        btnRun.setFocusPainted(false);
        btnRun.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(btnRun, gbc);

        add(controlPanel, BorderLayout.NORTH);

        // ========== CENTER: Visual Display ==========
        JPanel visualPanel = new JPanel(new BorderLayout(10, 10));
        visualPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Server Status Panel
        serverPanel = new JPanel(new BorderLayout());
        serverPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Server Status",
            TitledBorder.CENTER,
            TitledBorder.TOP
        ));
        serverPanel.setPreferredSize(new Dimension(200, 200));
        serverPanel.setBackground(new Color(255, 250, 205));
        
        lblServerStatus = new JLabel("IDLE", SwingConstants.CENTER);
        lblServerStatus.setFont(new Font("Arial", Font.BOLD, 24));
        lblServerStatus.setForeground(new Color(34, 139, 34));
        serverPanel.add(lblServerStatus, BorderLayout.CENTER);
        
        JLabel lblServerIcon = new JLabel("🏦", SwingConstants.CENTER);
        lblServerIcon.setFont(new Font("Arial", Font.PLAIN, 48));
        serverPanel.add(lblServerIcon, BorderLayout.NORTH);

        // Queue Panel
        queuePanel = new JPanel();
        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.X_AXIS));
        queuePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Queue (Waiting Customers)",
            TitledBorder.CENTER,
            TitledBorder.TOP
        ));
        queuePanel.setBackground(new Color(255, 248, 220));
        queuePanel.setPreferredSize(new Dimension(600, 200));
        queuePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        
        JScrollPane queueScroll = new JScrollPane(queuePanel);
        queueScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        JPanel serverAndQueue = new JPanel(new BorderLayout(10, 0));
        serverAndQueue.add(serverPanel, BorderLayout.WEST);
        serverAndQueue.add(queueScroll, BorderLayout.CENTER);

        // Live Stats Panel
        JPanel liveStatsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        liveStatsPanel.setBorder(BorderFactory.createTitledBorder("Live Statistics"));
        
        lblCurrentTime = createLiveStatLabel("Simulation Time: 0.00 min");
        lblCustomersServed = createLiveStatLabel("Customers Served: 0");
        lblCurrentQueueLength = createLiveStatLabel("Queue Length: 0");
        
        liveStatsPanel.add(lblCurrentTime);
        liveStatsPanel.add(lblCustomersServed);
        liveStatsPanel.add(lblCurrentQueueLength);

        visualPanel.add(serverAndQueue, BorderLayout.CENTER);
        visualPanel.add(liveStatsPanel, BorderLayout.SOUTH);

        add(visualPanel, BorderLayout.CENTER);

        // ========== BOTTOM: Statistics Output ==========
        txtStatistics = new JTextArea();
        txtStatistics.setEditable(false);
        txtStatistics.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtStatistics.setBorder(BorderFactory.createTitledBorder("Final Statistics"));
        JScrollPane sp = new JScrollPane(txtStatistics);
        sp.setPreferredSize(new Dimension(0, 200));
        add(sp, BorderLayout.SOUTH);

        // Action Listener
        btnRun.addActionListener(e -> runSimulation());
    }

    private JTextField createStyledTextField(String text, int columns) {
        JTextField tf = new JTextField(text, columns);
        tf.setFont(new Font("Arial", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return tf;
    }

    private JLabel createLiveStatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private void addControlRow(JPanel panel, GridBagConstraints gbc, int row, 
                               String label, JTextField... fields) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), gbc);
        
        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = i + 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(fields[i], gbc);
        }
    }

    private void runSimulation() {
        if (isRunning) return;
        isRunning = true;
        btnRun.setEnabled(false);
        btnRun.setText("⏳ Running...");
        
        // Clear previous simulation
        queuePanel.removeAll();
        customersInQueue.clear();
        currentCustomer = null;
        txtStatistics.setText("");
        
        try {
            int n = Integer.parseInt(txtNumCustomers.getText().trim());
            double arrMin = Double.parseDouble(txtArrivalMin.getText().trim());
            double arrMax = Double.parseDouble(txtArrivalMax.getText().trim());
            double svcMin = Double.parseDouble(txtServiceMin.getText().trim());
            double svcMax = Double.parseDouble(txtServiceMax.getText().trim());
            long seed = Long.parseLong(txtSeed.getText().trim());
            int speed = Integer.parseInt(txtSimulationSpeed.getText().trim());

            if (arrMin >= arrMax || svcMin >= svcMax) {
                throw new IllegalArgumentException("Min must be < Max");
            }

            Random rng = new Random(seed);
            
            // Generate data
            double[] arrivalTime = new double[n];
            double[] interArrival = new double[n];
            double[] serviceTime = new double[n];
            double[] serviceStart = new double[n];
            double[] waitInQueue = new double[n];
            double[] departureTime = new double[n];
            double[] timeInSystem = new double[n];

            arrivalTime[0] = uniform(rng, arrMin, arrMax);
            interArrival[0] = arrivalTime[0];
            for (int i = 1; i < n; i++) {
                interArrival[i] = uniform(rng, arrMin, arrMax);
                arrivalTime[i] = arrivalTime[i - 1] + interArrival[i];
            }

            for (int i = 0; i < n; i++) {
                serviceTime[i] = uniform(rng, svcMin, svcMax);
            }

            serviceStart[0] = arrivalTime[0];
            waitInQueue[0] = 0.0;
            departureTime[0] = serviceStart[0] + serviceTime[0];
            timeInSystem[0] = serviceTime[0];

            for (int i = 1; i < n; i++) {
                serviceStart[i] = Math.max(arrivalTime[i], departureTime[i - 1]);
                waitInQueue[i] = serviceStart[i] - arrivalTime[i];
                departureTime[i] = serviceStart[i] + serviceTime[i];
                timeInSystem[i] = waitInQueue[i] + serviceTime[i];
            }

            // Run animation
            runAnimation(n, arrivalTime, serviceTime, serviceStart, 
                        waitInQueue, departureTime, timeInSystem, interArrival, speed);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + ex.getMessage(), 
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
            isRunning = false;
            btnRun.setEnabled(true);
            btnRun.setText("▶ Run Simulation");
        }
    }

    private void runAnimation(int n, double[] arrivalTime, double[] serviceTime,
                             double[] serviceStart, double[] waitInQueue,
                             double[] departureTime, double[] timeInSystem,
                             double[] interArrival, int speed) {
        
        animationTimer = new Timer(speed, null);
        final int[] currentIndex = {0};
        final double[] currentTime = {0.0};
        final int[] customersServed = {0};
        final List<Integer> queueIndices = new ArrayList<>();
        final boolean[] serverBusy = {false};
        final double[] serviceRemaining = {0.0};

        animationTimer.addActionListener(e -> {
            if (currentIndex[0] >= n && queueIndices.isEmpty() && !serverBusy[0]) {
                animationTimer.stop();
                showFinalStatistics(n, arrivalTime, serviceTime, serviceStart,
                                  waitInQueue, departureTime, timeInSystem, interArrival);
                isRunning = false;
                btnRun.setEnabled(true);
                btnRun.setText("▶ Run Simulation");
                return;
            }

            // Add new arrivals
            while (currentIndex[0] < n && arrivalTime[currentIndex[0]] <= currentTime[0]) {
                queueIndices.add(currentIndex[0]);
                addCustomerToQueue(currentIndex[0]);
                currentIndex[0]++;
            }

            // Start service if server idle and queue not empty
            if (!serverBusy[0] && !queueIndices.isEmpty()) {
                int customerIdx = queueIndices.remove(0);
                removeCustomerFromQueue(0);
                startService(customerIdx, serviceTime[customerIdx]);
                serverBusy[0] = true;
                serviceRemaining[0] = serviceTime[customerIdx];
                updateServerStatus("BUSY", new Color(220, 20, 60));
            }

            // Process service
            if (serverBusy[0]) {
                double timeStep = speed / 1000.0 * 10; // Scale time
                serviceRemaining[0] -= timeStep;
                
                if (serviceRemaining[0] <= 0) {
                    serverBusy[0] = false;
                    customersServed[0]++;
                    removeCurrentCustomer();
                    updateServerStatus("IDLE", new Color(34, 139, 34));
                }
            }

            double timeStep = speed / 1000.0 * 10; // Scale time
            currentTime[0] += timeStep;
            updateLiveStats(currentTime[0], customersServed[0], queueIndices.size());
        });

        animationTimer.start();
    }

    private void addCustomerToQueue(int index) {
        CustomerVisual customer = new CustomerVisual(index + 1);
        customersInQueue.add(customer);
        queuePanel.add(customer);
        queuePanel.revalidate();
        queuePanel.repaint();
    }

    private void removeCustomerFromQueue(int position) {
        if (position < customersInQueue.size()) {
            CustomerVisual removed = customersInQueue.remove(position);
            queuePanel.remove(removed);
            queuePanel.revalidate();
            queuePanel.repaint();
        }
    }

    private void startService(int index, double serviceTime) {
        currentCustomer = new CustomerVisual(index + 1);
        currentCustomer.setBeingServed(true);
        serverPanel.add(currentCustomer, BorderLayout.SOUTH);
        serverPanel.revalidate();
        serverPanel.repaint();
    }

    private void removeCurrentCustomer() {
        if (currentCustomer != null) {
            serverPanel.remove(currentCustomer);
            currentCustomer = null;
            serverPanel.revalidate();
            serverPanel.repaint();
        }
    }

    private void updateServerStatus(String status, Color color) {
        lblServerStatus.setText(status);
        lblServerStatus.setForeground(color);
    }

    private void updateLiveStats(double time, int served, int queueLen) {
        lblCurrentTime.setText(String.format("Simulation Time: %.2f min", time));
        lblCustomersServed.setText("Customers Served: " + served);
        lblCurrentQueueLength.setText("Queue Length: " + queueLen);
    }

    private void showFinalStatistics(int n, double[] arrivalTime, double[] serviceTime,
                                    double[] serviceStart, double[] waitInQueue,
                                    double[] departureTime, double[] timeInSystem,
                                    double[] interArrival) {
        
        double sumWait = 0, sumTimeInSystem = 0, sumService = 0;
        int customersWhoWait = 0;

        for (int i = 0; i < n; i++) {
            sumWait += waitInQueue[i];
            sumTimeInSystem += timeInSystem[i];
            sumService += serviceTime[i];
            if (waitInQueue[i] > 0) customersWhoWait++;
        }

        double avgWait = sumWait / n;
        double avgTimeInSystem = sumTimeInSystem / n;
        double utilization = sumService / departureTime[n - 1];
        double probWait = (double) customersWhoWait / n;
        double avgQueueLength = sumWait / departureTime[n - 1];

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("      SIMULATION RESULTS\n");
        sb.append("========================================\n");
        sb.append(String.format("Number of customers           : %d\n", n));
        sb.append(String.format("Total simulation time (min)   : %.3f\n", departureTime[n-1]));
        sb.append("----------------------------------------\n");
        sb.append(String.format("Average wait in queue         : %.3f min\n", avgWait));
        sb.append(String.format("Average time in system        : %.3f min\n", avgTimeInSystem));
        sb.append(String.format("Average service time          : %.3f min\n", sumService/n));
        sb.append("----------------------------------------\n");
        sb.append(String.format("Server utilization            : %.2f%%\n", utilization*100));
        sb.append(String.format("Probability of waiting        : %.2f%%\n", probWait*100));
        sb.append(String.format("Customers who waited          : %d\n", customersWhoWait));
        sb.append("----------------------------------------\n");
        sb.append(String.format("Average # in queue (Lq)       : %.3f\n", avgQueueLength));
        sb.append("========================================\n");

        txtStatistics.setText(sb.toString());
    }

    private double uniform(Random rng, double a, double b) {
        return a + (b - a) * rng.nextDouble();
    }

    // Visual Customer Component
    class CustomerVisual extends JPanel {
        private int customerNumber;
        private boolean beingServed;

        public CustomerVisual(int number) {
            this.customerNumber = number;
            this.beingServed = false;
            setPreferredSize(new Dimension(60, 60));
            setMaximumSize(new Dimension(60, 60));
            setMinimumSize(new Dimension(60, 60));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            setBackground(new Color(255, 255, 255));
            setLayout(new BorderLayout());
            
            JLabel lblNum = new JLabel("C" + number, SwingConstants.CENTER);
            lblNum.setFont(new Font("Arial", Font.BOLD, 14));
            add(lblNum, BorderLayout.CENTER);
        }

        public void setBeingServed(boolean served) {
            this.beingServed = served;
            if (served) {
                setBackground(new Color(144, 238, 144));
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(34, 139, 34), 3),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new BankSim().setVisible(true);
        });
    }
}