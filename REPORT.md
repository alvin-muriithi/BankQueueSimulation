# Simulation Report: Single-Server Bank Queue System

## 1. Introduction
Queueing theory is a critical branch of operations research used to analyze waiting lines. This project simulates a single-server bank queue to evaluate system performance under specific stochastic conditions. By simulating 100 customers with randomized arrival and service times, we can estimate real-world metrics such as customer wait times, teller utilization, and queue congestion without disrupting actual bank operations.

## 2. System Model and Assumptions
The simulation is built on the following assumptions:
- **Single Server:** The bank has only one active teller.
- **Queue Discipline:** First-In-First-Out (FIFO). Customers are served in the exact order they arrive.
- **Inter-arrival Times:** Generated using a Continuous Uniform Distribution $U(0.5, 3.0)$ minutes.
- **Service Times:** Generated using a Continuous Uniform Distribution $U(1.0, 4.0)$ minutes.
- **Infinite Capacity:** The queue can hold an unlimited number of customers.

## 3. Simulation Methodology
The system uses a discrete-event simulation approach. The logic for each customer $i$ is calculated as follows:

1. **Random Variate Generation:** Uniform random numbers are generated using Java's `Random` class and scaled to the specified bounds: 
   $X = a + (b - a) \times U(0,1)$
2. **Event Logic:**
   - **Arrival Time:** Cumulative sum of inter-arrival times.
   - **Service Start Time:** The server can only start when the customer arrives AND the previous customer has left. 
     `Service Start = max(Arrival Time[i], Departure Time[i-1])`
   - **Wait Time:** `Service Start - Arrival Time`
   - **Departure Time:** `Service Start + Service Time`

## 4. Key Metrics and Interpretation of Findings
The simulation outputs several key statistics. Here is what they mean in a real-world context:

- **Server Utilization:** Represents the percentage of time the teller is actively working. 
  *Meaning:* If utilization is consistently above 80-85%, the bank is operating near capacity. Any slight increase in customer arrivals will cause the queue to grow exponentially.
- **Probability of Waiting:** The percentage of customers who do not get served immediately upon arrival. 
  *Meaning:* A high probability indicates poor customer experience and suggests the need for an additional teller.
- **Average Wait in Queue ($W_q$):** The average time a customer spends waiting before being served. 
  *Meaning:* This is the primary metric for customer satisfaction. 
- **Average Number in Queue ($L_q$):** Calculated using Little's Law ($L_q = \lambda \times W_q$). It represents the average physical length of the line.

### Observation on System Load
Because the maximum service time (4.0 mins) is greater than the maximum inter-arrival time (3.0 mins), the system is heavily loaded. In a real-world scenario, this specific configuration would inevitably lead to long queues and high wait times, proving that a single teller is insufficient for this arrival rate.

## 5. Conclusion
Discrete-event simulation is a powerful tool for capacity planning. By visualizing the queue and calculating statistical metrics, bank managers can make data-driven decisions about staffing levels. The visual GUI developed in this project successfully bridges the gap between abstract queueing formulas and observable system behavior.

## 6. References and Sources
1. **Banks, J., Carson, J. S., Nelson, B. L., & Nicol, D. M. (2010).** *Discrete-Event System Simulation* (5th ed.). Pearson. (Used for foundational simulation logic and event scheduling).
2. **Gross, D., & Harris, C. M. (1998).** *Fundamentals of Queueing Theory* (3rd ed.). Wiley. (Used for queueing metrics and Little's Law interpretations).
3. **Oracle Documentation.** *Java Platform, Standard Edition 21 API Specification - java.util.Random & javax.swing*. (Used for random variate generation and GUI development).
4. **Kendall, D. G. (1953).** "Stochastic Problems in Queues and Dam Theory". *Journal of the Royal Statistical Society*. (Historical basis for queueing notation and theory).